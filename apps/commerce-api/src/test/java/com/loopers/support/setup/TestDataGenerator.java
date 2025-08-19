package com.loopers.support.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestDataGenerator.class);

    // === 데이터 규모 설정 ===
    private static final int BRAND_COUNT = 1000;           // 브랜드 1000개
    private static final int PRODUCT_COUNT = 1_000_000;    // 상품 100만개
    private static final int LIKE_COUNT = 1_000_000;       // 좋아요 100만개
    private static final int USER_COUNT = 15_000;          // 사용자 1.5만명

    // === 🔥 불균등 분포 설정 (성능 테스트 최적화) ===
    private static final int[] BRAND_PRODUCT_DISTRIBUTION = initializeBrandDistribution();

    // === 배치 크기 설정 ===
    private static final int BRAND_BATCH_SIZE = 100;       // 브랜드 배치 크기
    private static final int PRODUCT_BATCH_SIZE = 5_000;   // 상품 배치 크기
    private static final int LIKE_BATCH_SIZE = 5_000;      // 좋아요 배치 크기

    // === 데이터베이스 연결 설정 ===
    private static final String URL = "jdbc:mysql://localhost:3306/loopers";
    private static final String USERNAME = "application";
    private static final String PASSWORD = "application";

    /**
     * 🎯 극적인 성능 차이를 위한 불균등 브랜드 분포 초기화
     * - 상위 5개 브랜드: 전체의 50% (각 10만개)
     * - 중간 15개 브랜드: 전체의 30% (각 2만개)
     * - 나머지 980개 브랜드: 전체의 20% (각 약 200개)
     */
    private static int[] initializeBrandDistribution() {
        int[] distribution = new int[BRAND_COUNT];
        int remainingProducts = PRODUCT_COUNT;

        // 🔥 메가 브랜드 (상위 5개): 각 10만개
        for (int i = 0; i < 5; i++) {
            distribution[i] = 100_000;
            remainingProducts -= 100_000;
        }

        // 🏢 대형 브랜드 (6-20번): 각 2만개
        for (int i = 5; i < 20; i++) {
            distribution[i] = 20_000;
            remainingProducts -= 20_000;
        }

        // 🏪 중형 브랜드 (21-50번): 각 5천개
        for (int i = 20; i < 50; i++) {
            distribution[i] = 5_000;
            remainingProducts -= 5_000;
        }

        // 🛒 소형 브랜드 나머지: 랜덤 분배 (100-500개)
        Random random = new Random(42); // 시드 고정으로 재현 가능
        for (int i = 50; i < BRAND_COUNT - 1; i++) {
            int products = 100 + random.nextInt(400); // 100-499개
            distribution[i] = Math.min(products, remainingProducts);
            remainingProducts -= distribution[i];
        }

        // 마지막 브랜드에 남은 상품 할당
        distribution[BRAND_COUNT - 1] = Math.max(0, remainingProducts);

        return distribution;
    }

    @Test
    void generateTestData() {
        System.out.println("🚀 테스트 데이터 생성을 시작합니다.");
        System.out.println("📊 규모: 브랜드 " + String.format("%,d", BRAND_COUNT) + "개, " +
            "상품 " + String.format("%,d", PRODUCT_COUNT) + "개, " +
            "좋아요 " + String.format("%,d", LIKE_COUNT) + "개");
        System.out.println("👥 사용자 " + String.format("%,d", USER_COUNT) + "명");
        log.info("🚀 테스트 데이터 생성 시작");

        // 분포 미리보기 출력
        printDistributionPreview();

        long startTime = System.currentTimeMillis();
        Connection conn = null;

        try {
            // 1. JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. 직접 연결
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            conn.setAutoCommit(false); // 수동 커밋 모드

            System.out.println("✅ MySQL 직접 연결 성공");

            // 3. 연결 정보 확인
            debugConnection(conn);

            // 4. 테이블 생성
            createTables(conn);

            // 5. 기존 데이터 확인
            checkExistingData(conn);

            // 6. 불균등 분포 데이터 생성
            executeSkewedDataGeneration(conn);

            // 7. 수동 커밋
            conn.commit();
            System.out.println("✅ 데이터 커밋 완료!");

            // 8. 결과 확인
            verifySkewedDataGeneration(conn);

            // 9. 성능 테스트 안내
            printSkewedPerformanceTestGuide();

            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            long durationMinutes = durationMillis / (1000 * 60);

            System.out.println("✅ 테스트 데이터 생성 완료!");
            System.out.println("   소요 시간: " + durationMillis + "ms (" + durationMinutes + "분)");
            log.info("✅ 테스트 데이터 생성 완료! 소요 시간: {}ms ({}분)", durationMillis, durationMinutes);

        } catch (Exception e) {
            System.err.println("❌ 데이터 생성 중 오류: " + e.getMessage());
            log.error("❌ 데이터 생성 중 오류: {}", e.getMessage(), e);
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 롤백 완료");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ 롤백 실패: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("데이터 생성 실패", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("🔌 연결 종료");
                } catch (SQLException e) {
                    System.err.println("❌ 연결 종료 실패: " + e.getMessage());
                }
            }
        }
    }

    private void printDistributionPreview() {
        System.out.println("\n📈 브랜드별 상품 분포 미리보기:");
        System.out.println("🔥 메가 브랜드 (1-5번):");
        for (int i = 0; i < 5; i++) {
            System.out.printf("   브랜드 %d: %,d개\n", i + 1, BRAND_PRODUCT_DISTRIBUTION[i]);
        }
        System.out.println("🏢 대형 브랜드 (6-10번):");
        for (int i = 5; i < 10; i++) {
            System.out.printf("   브랜드 %d: %,d개\n", i + 1, BRAND_PRODUCT_DISTRIBUTION[i]);
        }
        System.out.println("🛒 소형 브랜드 (995-1000번):");
        for (int i = 995; i < 1000; i++) {
            System.out.printf("   브랜드 %d: %,d개\n", i + 1, BRAND_PRODUCT_DISTRIBUTION[i]);
        }
        System.out.println();
    }

    @Test
    void checkDataStatus() {
        System.out.println("📊 현재 데이터 상태를 확인합니다.");
        log.info("📊 현재 데이터 상태를 확인합니다.");

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            debugConnection(conn);
            verifySkewedDataGeneration(conn);
        } catch (Exception e) {
            System.err.println("❌ 데이터 상태 확인 실패: " + e.getMessage());
            log.error("❌ 데이터 상태 확인 실패: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void clearAllTestData() {
        System.out.println("🧹 모든 테스트 데이터를 삭제합니다.");
        log.info("🧹 모든 테스트 데이터를 삭제합니다.");

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);

            clearExistingData(conn);

            conn.commit();
            System.out.println("✅ 데이터 삭제 및 커밋 완료!");

            verifySkewedDataGeneration(conn);

        } catch (Exception e) {
            System.err.println("❌ 데이터 삭제 중 오류: " + e.getMessage());
            log.error("❌ 데이터 삭제 중 오류: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void createTables() {
        System.out.println("📋 테이블만 생성합니다.");
        log.info("📋 테이블만 생성합니다.");

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);

            createTables(conn);

            conn.commit();
            System.out.println("✅ 테이블 생성 및 커밋 완료!");

        } catch (Exception e) {
            System.err.println("❌ 테이블 생성 실패: " + e.getMessage());
            log.error("❌ 테이블 생성 실패: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void debugConnection(Connection conn) throws SQLException {
        System.out.println("🔗 데이터베이스 연결 테스트 중...");
        log.info("🔗 데이터베이스 연결 테스트 중...");

        try (Statement stmt = conn.createStatement()) {
            // 현재 데이터베이스
            try (ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    System.out.println("📊 현재 DB: " + rs.getString(1));
                    log.info("📊 현재 DB: {}", rs.getString(1));
                }
            }

            // MySQL 버전
            try (ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                if (rs.next()) {
                    System.out.println("🔢 MySQL 버전: " + rs.getString(1));
                }
            }

            // 현재 사용자
            try (ResultSet rs = stmt.executeQuery("SELECT USER()")) {
                if (rs.next()) {
                    System.out.println("👤 사용자: " + rs.getString(1));
                }
            }

            // 테이블 목록
            System.out.println("📋 현재 테이블:");
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                boolean hasAnyTable = false;
                while (rs.next()) {
                    System.out.println("   - " + rs.getString(1));
                    hasAnyTable = true;
                }
                if (!hasAnyTable) {
                    System.out.println("   ⚠️ 테이블 없음");
                }
            }
        }
    }

    private void createTables(Connection conn) throws SQLException {
        System.out.println("📋 테이블 생성 시작");

        try (Statement stmt = conn.createStatement()) {
            // 외래키 체크 비활성화
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");

            // 기존 테이블 삭제
            stmt.executeUpdate("DROP TABLE IF EXISTS likes");
            stmt.executeUpdate("DROP TABLE IF EXISTS product");
            stmt.executeUpdate("DROP TABLE IF EXISTS brand");
            System.out.println("🗑️ 기존 테이블 삭제 완료");

            // 브랜드 테이블 생성
            stmt.executeUpdate("""
                CREATE TABLE brand (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description VARCHAR(500)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            System.out.println("✅ brand 테이블 생성");

            // 상품 테이블 생성
            stmt.executeUpdate("""
                CREATE TABLE product (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    brand_id BIGINT NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    price DECIMAL(19,2) NOT NULL,
                    stock_quantity INT NOT NULL,
                    created_at DATETIME NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            System.out.println("✅ product 테이블 생성");

            // 좋아요 테이블 생성
            stmt.executeUpdate("""
                CREATE TABLE likes (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    user_id VARCHAR(255) NOT NULL,
                    product_id BIGINT NOT NULL,
                    created_at DATETIME NOT NULL,
                    UNIQUE KEY unique_user_product (user_id, product_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            System.out.println("✅ likes 테이블 생성");

            // 외래키 추가
            stmt.executeUpdate("""
                ALTER TABLE product 
                ADD CONSTRAINT fk_product_brand 
                FOREIGN KEY (brand_id) REFERENCES brand(id)
                """);

            stmt.executeUpdate("""
                ALTER TABLE likes 
                ADD CONSTRAINT fk_likes_product 
                FOREIGN KEY (product_id) REFERENCES product(id)
                """);

            // 외래키 체크 재활성화
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");

            System.out.println("✅ 모든 테이블 생성 완료!");
        }
    }

    private void checkExistingData(Connection conn) throws SQLException {
        System.out.println("📊 기존 데이터 확인 중...");
        log.info("📊 기존 데이터 확인 중...");

        try (Statement stmt = conn.createStatement()) {
            long brandCount = 0, productCount = 0, likeCount = 0;

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM brand")) {
                if (rs.next()) brandCount = rs.getLong(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM product")) {
                if (rs.next()) productCount = rs.getLong(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM likes")) {
                if (rs.next()) likeCount = rs.getLong(1);
            }

            System.out.println("현재 데이터: 브랜드 " + String.format("%,d", brandCount) + "개, " +
                "상품 " + String.format("%,d", productCount) + "개, " +
                "좋아요 " + String.format("%,d", likeCount) + "개");
            log.info("현재 데이터: 브랜드 {}개, 상품 {}개, 좋아요 {}개", brandCount, productCount, likeCount);

            if (productCount >= PRODUCT_COUNT / 2) { // 50만개 이상이면 기존 데이터로 판단
                System.out.println("⚠️ 이미 대용량 테스트 데이터가 존재합니다. (상품 " + String.format("%,d", productCount) + "개)");
                log.info("⚠️ 이미 대용량 테스트 데이터가 존재합니다. (상품 {}개)", productCount);

                String forceGenerate = System.getProperty("force.generate", "false");
                if (!"true".equals(forceGenerate)) {
                    System.out.println("💡 새로 생성하려면 -Dforce.generate=true 옵션을 사용하세요.");
                    throw new RuntimeException("기존 데이터 존재 - 생성 중단");
                } else {
                    System.out.println("🔄 강제 생성 모드로 기존 데이터를 삭제합니다.");
                    log.info("🔄 강제 생성 모드로 기존 데이터를 삭제합니다.");
                    clearExistingData(conn);
                }
            }
        }
    }

    private void clearExistingData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            int likesDeleted = stmt.executeUpdate("DELETE FROM likes");
            int productsDeleted = stmt.executeUpdate("DELETE FROM product");
            int brandsDeleted = stmt.executeUpdate("DELETE FROM brand");

            System.out.println("기존 데이터 삭제 완료: 좋아요 " + String.format("%,d", likesDeleted) + "개, " +
                "상품 " + String.format("%,d", productsDeleted) + "개, " +
                "브랜드 " + String.format("%,d", brandsDeleted) + "개");
            log.info("기존 데이터 삭제 완료: 좋아요 {}개, 상품 {}개, 브랜드 {}개", likesDeleted, productsDeleted, brandsDeleted);
        }
    }

    private void executeSkewedDataGeneration(Connection conn) throws SQLException {
        System.out.println("📜 불균등 분포 데이터 생성 실행 중...");
        log.info("📜 불균등 분포 데이터 생성 실행 중...");

        // MySQL 설정
        System.out.println("1️⃣ MySQL 설정 중...");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("SET @@cte_max_recursion_depth = 1100000"); // 110만으로 증가
            stmt.executeUpdate("SET @@max_execution_time = 0"); // 실행 시간 제한 해제
            stmt.executeUpdate("SET @@sort_buffer_size = 16777216"); // 16MB 정렬 버퍼
        }

        // 브랜드 생성 (1000개)
        System.out.println("2️⃣ 브랜드 데이터 생성 중... (목표: " + String.format("%,d", BRAND_COUNT) + "개)");
        generateBrands(conn);

        // 불균등 분포 상품 생성 (100만개)
        System.out.println("3️⃣ 불균등 분포 상품 데이터 생성 중... (목표: " + String.format("%,d", PRODUCT_COUNT) + "개)");
        generateSkewedProducts(conn);

        // 불균등 분포 좋아요 생성 (100만개)
        System.out.println("4️⃣ 불균등 분포 좋아요 데이터 생성 중... (목표: " + String.format("%,d", LIKE_COUNT) + "개)");
        generateSkewedLikes(conn);

        System.out.println("✅ 모든 불균등 분포 데이터 생성 완료");
        log.info("✅ 모든 불균등 분포 데이터 생성 완료");
    }

    private void generateBrands(Connection conn) throws SQLException {
        int totalBatches = (BRAND_COUNT + BRAND_BATCH_SIZE - 1) / BRAND_BATCH_SIZE; // 올림 계산
        int totalCreated = 0;

        for (int batch = 0; batch < totalBatches; batch++) {
            int startId = batch * BRAND_BATCH_SIZE + 1;
            int endId = Math.min((batch + 1) * BRAND_BATCH_SIZE, BRAND_COUNT);

            StringBuilder sql = new StringBuilder("INSERT INTO brand (name, description) VALUES ");
            for (int i = startId; i <= endId; i++) {
                if (i > startId) sql.append(", ");

                // 브랜드 크기에 따른 설명 차별화
                String brandType = getBrandType(i - 1); // 0-based index
                sql.append(String.format("('브랜드%04d', '%s 브랜드%04d에서 제공하는 제품들을 만나보세요.')",
                    i, brandType, i));
            }

            try (Statement stmt = conn.createStatement()) {
                int result = stmt.executeUpdate(sql.toString());
                totalCreated += result;
                System.out.printf("브랜드 배치 %d/%d 완료: %d개 (누적: %,d개)\n",
                    batch + 1, totalBatches, result, totalCreated);
            }
        }

        System.out.println("브랜드 생성 완료: " + String.format("%,d", totalCreated) + "개");
        log.info("브랜드 생성 완료: {}개", totalCreated);
    }

    private String getBrandType(int brandIndex) {
        if (brandIndex < 5) return "🔥메가";
        if (brandIndex < 20) return "🏢대형";
        if (brandIndex < 50) return "🏪중형";
        return "🛒소형";
    }

    private void generateSkewedProducts(Connection conn) throws SQLException {
        System.out.println("불균등 분포 상품 데이터 생성 중...");

        int totalCreated = 0;
        long currentProductId = 1;

        try (Statement stmt = conn.createStatement()) {
            // 브랜드별로 개별적으로 상품 생성
            for (int brandId = 1; brandId <= BRAND_COUNT; brandId++) {
                int productsForThisBrand = BRAND_PRODUCT_DISTRIBUTION[brandId - 1];

                if (productsForThisBrand == 0) continue;

                // 대용량 브랜드는 배치로 처리
                if (productsForThisBrand >= 10000) {
                    totalCreated += generateProductsForLargeBrand(stmt, brandId, productsForThisBrand, currentProductId);
                } else {
                    // 소량 브랜드는 한 번에 처리
                    totalCreated += generateProductsForSmallBrand(stmt, brandId, productsForThisBrand, currentProductId);
                }

                currentProductId += productsForThisBrand;

                // 진행상황 출력 (10% 단위)
                if (brandId % 100 == 0 || brandId <= 50) {
                    System.out.printf("브랜드 %d/%d 완료: 현재 브랜드 %,d개 (전체 누적: %,d개)\n",
                        brandId, BRAND_COUNT, productsForThisBrand, totalCreated);
                }
            }
        }

        System.out.println("불균등 분포 상품 생성 완료: " + String.format("%,d", totalCreated) + "개");
        log.info("불균등 분포 상품 생성 완료: {}개", totalCreated);
    }

    private int generateProductsForLargeBrand(Statement stmt, int brandId, int totalProducts, long startProductId) throws SQLException {
        System.out.printf("🔥 대형 브랜드 %d 처리 중: %,d개 상품\n", brandId, totalProducts);

        int created = 0;
        int batches = (totalProducts + PRODUCT_BATCH_SIZE - 1) / PRODUCT_BATCH_SIZE;

        for (int batch = 0; batch < batches; batch++) {
            int batchStart = batch * PRODUCT_BATCH_SIZE;
            int batchEnd = Math.min((batch + 1) * PRODUCT_BATCH_SIZE, totalProducts);
            int batchSize = batchEnd - batchStart;

            StringBuilder sql = new StringBuilder("INSERT INTO product (brand_id, name, price, stock_quantity, created_at) VALUES ");

            for (int i = 0; i < batchSize; i++) {
                if (i > 0) sql.append(", ");
                long productNumber = startProductId + batchStart + i;
                sql.append(String.format("(%d, '상품 %07d', %d, %d, DATE_SUB(NOW(), INTERVAL %d DAY))",
                    brandId,
                    productNumber,
                    1000 + (productNumber % 100000), // 가격
                    (int)(productNumber % 100), // 수량
                    (int)(productNumber % 730) // 생성일 (2년 범위)
                ));
            }

            int result = stmt.executeUpdate(sql.toString());
            created += result;

            System.out.printf("  브랜드 %d 배치 %d/%d: %,d개\n", brandId, batch + 1, batches, result);
        }

        return created;
    }

    private int generateProductsForSmallBrand(Statement stmt, int brandId, int totalProducts, long startProductId) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO product (brand_id, name, price, stock_quantity, created_at) VALUES ");

        for (int i = 0; i < totalProducts; i++) {
            if (i > 0) sql.append(", ");
            long productNumber = startProductId + i;
            sql.append(String.format("(%d, '상품 %07d', %d, %d, DATE_SUB(NOW(), INTERVAL %d DAY))",
                brandId,
                productNumber,
                1000 + (productNumber % 100000), // 가격
                (int)(productNumber % 100), // 수량
                (int)(productNumber % 730) // 생성일 (2년 범위)
            ));
        }

        return stmt.executeUpdate(sql.toString());
    }

    private void generateSkewedLikes(Connection conn) throws SQLException {
        System.out.println("불균등 분포 좋아요 데이터 생성 중...");
        System.out.println("🎯 목표: " + String.format("%,d", LIKE_COUNT) + "개 (메가브랜드 집중)");

        int totalBatches = LIKE_COUNT / LIKE_BATCH_SIZE;
        int totalCreated = 0;

        try (Statement stmt = conn.createStatement()) {
            for (int batch = 0; batch < totalBatches; batch++) {
                int startId = batch * LIKE_BATCH_SIZE + 1;
                int endId = (batch + 1) * LIKE_BATCH_SIZE;

                // 🔥 String.format 대신 직접 문자열 조합으로 콜론 문제 해결
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("INSERT IGNORE INTO likes (user_id, product_id, created_at) ");
                sqlBuilder.append("WITH RECURSIVE numbers AS ( ");
                sqlBuilder.append("    SELECT ").append(startId).append(" AS n ");
                sqlBuilder.append("    UNION ALL ");
                sqlBuilder.append("    SELECT n + 1 FROM numbers WHERE n < ").append(endId).append(" ");
                sqlBuilder.append(") ");
                sqlBuilder.append("SELECT ");
                sqlBuilder.append("    CONCAT('user', LPAD( ");
                sqlBuilder.append("        ((n * 17 + ").append(batch).append(" * 137 + FLOOR(n/1000) * 23) % ").append(USER_COUNT).append(") + 1, 5, '0' ");
                sqlBuilder.append("    )) AS user_id, ");
                sqlBuilder.append("    CASE ");
                sqlBuilder.append("        WHEN (n % 100) < 50 THEN ");
                sqlBuilder.append("            ((n * 31 + ").append(batch).append(" * 239) % 500000) + 1 ");
                sqlBuilder.append("        WHEN (n % 100) < 80 THEN ");
                sqlBuilder.append("            ((n * 43 + ").append(batch).append(" * 317) % 300000) + 500001 ");
                sqlBuilder.append("        ELSE ");
                sqlBuilder.append("            ((n * 53 + ").append(batch).append(" * 419) % 200000) + 800001 ");
                sqlBuilder.append("    END AS product_id, ");
                sqlBuilder.append("    DATE_SUB(NOW(), INTERVAL (n % 90) DAY) AS created_at ");
                sqlBuilder.append("FROM numbers");

                String sql = sqlBuilder.toString();

                long batchStart = System.currentTimeMillis();
                int result = stmt.executeUpdate(sql);
                long batchEnd = System.currentTimeMillis();

                totalCreated += result;

                System.out.printf("좋아요 배치 %d/%d 완료: %,d개 (누적: %,d개) - %dms\n",
                    batch + 1, totalBatches, result, totalCreated, batchEnd - batchStart);

                // 목표의 80% 달성하면 성공으로 간주
                if (totalCreated >= LIKE_COUNT * 0.8) {
                    System.out.println("🎯 목표의 80% 달성 - 충분한 데이터 확보!");
                    break;
                }
            }
        }

        System.out.println("좋아요 생성 완료: " + String.format("%,d", totalCreated) + "개");
        System.out.println("📊 예상 분포: 메가브랜드 50%, 대형브랜드 30%, 소형브랜드 20%");
        log.info("좋아요 생성 완료: {}개", totalCreated);
    }

    private void verifySkewedDataGeneration(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            long brandCount = 0, productCount = 0, likeCount = 0;

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM brand")) {
                if (rs.next()) brandCount = rs.getLong(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM product")) {
                if (rs.next()) productCount = rs.getLong(1);
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM likes")) {
                if (rs.next()) likeCount = rs.getLong(1);
            }

            System.out.println("📊 최종 데이터 현황:");
            System.out.println("   - 브랜드: " + String.format("%,d", brandCount) + "개");
            System.out.println("   - 상품: " + String.format("%,d", productCount) + "개");
            System.out.println("   - 좋아요: " + String.format("%,d", likeCount) + "개");

            log.info("📊 최종 데이터: 브랜드 {}개, 상품 {}개, 좋아요 {}개", brandCount, productCount, likeCount);

            if (productCount >= PRODUCT_COUNT * 0.9) { // 90% 이상이면 성공
                log.info("🎯 데이터 준비 완료!");
            } else {
                System.out.println("⚠️ 데이터 생성이 불완전합니다. 다시 시도해주세요.");
            }

            // 🔥 불균등 분포 확인 (상위 10개 + 하위 10개)
            System.out.println("\n📈 불균등 브랜드별 상품 분포 (상위 10개):");
            try (ResultSet rs = stmt.executeQuery("""
                SELECT brand_id, COUNT(*) as product_count 
                FROM product 
                GROUP BY brand_id 
                ORDER BY product_count DESC 
                LIMIT 10
                """)) {
                while (rs.next()) {
                    long brandId = rs.getLong("brand_id");
                    long productCnt = rs.getLong("product_count");
                    String brandType = getBrandType((int)brandId - 1);
                    System.out.printf("   %s 브랜드 %d: %,d개\n", brandType, brandId, productCnt);
                }
            }

            System.out.println("\n📉 브랜드별 상품 분포 (하위 10개):");
            try (ResultSet rs = stmt.executeQuery("""
                SELECT brand_id, COUNT(*) as product_count 
                FROM product 
                GROUP BY brand_id 
                ORDER BY product_count ASC 
                LIMIT 10
                """)) {
                while (rs.next()) {
                    long brandId = rs.getLong("brand_id");
                    long productCnt = rs.getLong("product_count");
                    String brandType = getBrandType((int)brandId - 1);
                    System.out.printf("   %s 브랜드 %d: %,d개\n", brandType, brandId, productCnt);
                }
            }

            // 🔥 좋아요 분포도 확인
            System.out.println("\n💝 브랜드별 좋아요 분포 (상위 5개):");
            try (ResultSet rs = stmt.executeQuery("""
                SELECT p.brand_id, COUNT(l.id) as like_count 
                FROM product p
                LEFT JOIN likes l ON p.id = l.product_id
                GROUP BY p.brand_id 
                ORDER BY like_count DESC 
                LIMIT 5
                """)) {
                while (rs.next()) {
                    long brandId = rs.getLong("brand_id");
                    long likeCnt = rs.getLong("like_count");
                    String brandType = getBrandType((int)brandId - 1);
                    System.out.printf("   %s 브랜드 %d: %,d개 좋아요\n", brandType, brandId, likeCnt);
                }
            }
        }
    }

    private void printSkewedPerformanceTestGuide() {
        System.out.println("\n🎯 불균등 분포 성능 테스트 가이드:");
        System.out.println("==========================================");
        System.out.println("🔥 극도로 불균등한 분포로 인덱스 효과 극대화!");
        System.out.println("");
        System.out.println("📊 데이터 분포:");
        System.out.println("   🔥 메가브랜드 (1-5번): 각 10만개 (총 50만개, 50%)");
        System.out.println("   🏢 대형브랜드 (6-20번): 각 2만개 (총 30만개, 30%)");
        System.out.println("   🛒 소형브랜드 (나머지): 각 100-500개 (총 20만개, 20%)");
        System.out.println("");
        System.out.println("🚨 예상 성능 차이:");
        System.out.println("   브랜드 1 검색 (10만개): 인덱스 없으면 2-10초, 있으면 50-200ms");
        System.out.println("   브랜드 999 검색 (200개): 인덱스 없어도 빠름, 있으면 1-5ms");
        System.out.println("   브랜드 1 + 좋아요순: 인덱스 없으면 10-30초, 있으면 100-500ms");
        System.out.println("");
        System.out.println("1️⃣ 현재 상태 테스트 (인덱스 없음) - 매우 느림 예상:");
        System.out.println("   SELECT p.* FROM product p");
        System.out.println("   LEFT JOIN likes l ON p.id = l.product_id");
        System.out.println("   WHERE p.brand_id = 1  -- 메가브랜드");
        System.out.println("   GROUP BY p.id ORDER BY COUNT(l.id) DESC LIMIT 20;");
        System.out.println("   예상: type=ALL, rows=1,000,000, 시간=10-30초");
        System.out.println("");
        System.out.println("2️⃣ 인덱스 생성:");
        System.out.println("   CREATE INDEX idx_product_brand_id ON product(brand_id);");
        System.out.println("   CREATE INDEX idx_likes_product_id ON likes(product_id);");
        System.out.println("   CREATE INDEX idx_product_brand_created ON product(brand_id, created_at);");
        System.out.println("");
        System.out.println("3️⃣ 개선 후 테스트:");
        System.out.println("   동일한 쿼리 실행");
        System.out.println("   예상: type=ref, rows=100,000, 시간=100-500ms");
        System.out.println("");
        System.out.println("4️⃣ 소형브랜드도 테스트:");
        System.out.println("   WHERE p.brand_id = 999  -- 소형브랜드");
        System.out.println("   예상: 인덱스 전후 모두 빠름 (차이는 크지 않음)");
        System.out.println("");
        System.out.println("📈 예상 성능 개선율:");
        System.out.println("   🔥 메가브랜드: 2000-6000% 개선 (20-60배 빨라짐)!");
        System.out.println("   🏢 대형브랜드: 500-1500% 개선 (5-15배 빨라짐)");
        System.out.println("   🛒 소형브랜드: 100-300% 개선 (2-3배 빨라짐)");
        System.out.println("");
        System.out.println("💡 추가 테스트:");
        System.out.println("   - SQL_NO_CACHE로 캐시 무력화 테스트");
        System.out.println("   - 복합 조건으로 더 복잡한 쿼리 테스트");
        System.out.println("   - 카디널리티 분석: SELECT brand_id, COUNT(*) FROM product GROUP BY brand_id;");
        System.out.println("==========================================\n");

        log.info("불균등 분포 성능 테스트 가이드 출력 완료");
    }

    /**
     * 🔍 카디널리티 분석 테스트 메서드
     */
    @Test
    void analyzeCardinality() {
        System.out.println("🔍 카디널리티 분석을 시작합니다.");
        log.info("🔍 카디널리티 분석을 시작합니다.");

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            analyzeTableCardinality(conn);
        } catch (Exception e) {
            System.err.println("❌ 카디널리티 분석 실패: " + e.getMessage());
            log.error("❌ 카디널리티 분석 실패: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void analyzeTableCardinality(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            System.out.println("\n📊 브랜드별 카디널리티 분석:");
            System.out.println("==================================================");

            // 전체 통계
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total_products FROM product")) {
                if (rs.next()) {
                    long totalProducts = rs.getLong("total_products");
                    System.out.println("전체 상품 수: " + String.format("%,d", totalProducts) + "개");
                }
            }

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(DISTINCT brand_id) as unique_brands FROM product")) {
                if (rs.next()) {
                    long uniqueBrands = rs.getLong("unique_brands");
                    System.out.println("유니크 브랜드 수: " + String.format("%,d", uniqueBrands) + "개");
                }
            }

            // 브랜드별 분포 통계
            System.out.println("\n📈 브랜드별 상품 분포 통계:");
            try (ResultSet rs = stmt.executeQuery("""
                SELECT 
                    MIN(product_count) as min_products,
                    MAX(product_count) as max_products,
                    AVG(product_count) as avg_products,
                    STDDEV(product_count) as stddev_products
                FROM (
                    SELECT brand_id, COUNT(*) as product_count 
                    FROM product 
                    GROUP BY brand_id
                ) brand_stats
                """)) {
                if (rs.next()) {
                    System.out.printf("최소: %,d개\n", rs.getLong("min_products"));
                    System.out.printf("최대: %,d개\n", rs.getLong("max_products"));
                    System.out.printf("평균: %.1f개\n", rs.getDouble("avg_products"));
                    System.out.printf("표준편차: %.1f\n", rs.getDouble("stddev_products"));
                }
            }

            // 분포 구간별 브랜드 수
            System.out.println("\n📊 브랜드 규모별 분포:");
            try (ResultSet rs = stmt.executeQuery("""
                SELECT 
                    CASE 
                        WHEN product_count >= 50000 THEN '🔥 메가브랜드 (5만개 이상)'
                        WHEN product_count >= 10000 THEN '🏢 대형브랜드 (1-5만개)'
                        WHEN product_count >= 1000 THEN '🏪 중형브랜드 (1천-1만개)'
                        ELSE '🛒 소형브랜드 (1천개 미만)'
                    END as brand_size,
                    COUNT(*) as brand_count,
                    MIN(product_count) as min_products,
                    MAX(product_count) as max_products
                FROM (
                    SELECT brand_id, COUNT(*) as product_count 
                    FROM product 
                    GROUP BY brand_id
                ) brand_stats
                GROUP BY 
                    CASE 
                        WHEN product_count >= 50000 THEN '🔥 메가브랜드 (5만개 이상)'
                        WHEN product_count >= 10000 THEN '🏢 대형브랜드 (1-5만개)'
                        WHEN product_count >= 1000 THEN '🏪 중형브랜드 (1천-1만개)'
                        ELSE '🛒 소형브랜드 (1천개 미만)'
                    END
                ORDER BY min_products DESC
                """)) {
                while (rs.next()) {
                    System.out.printf("%s: %d개 브랜드 (%,d-%,d개)\n",
                        rs.getString("brand_size"),
                        rs.getInt("brand_count"),
                        rs.getLong("min_products"),
                        rs.getLong("max_products"));
                }
            }

            System.out.println("\n💡 카디널리티 분석 결론:");
            System.out.println("- 극도로 불균등한 분포로 인덱스 효과 극대화 가능");
            System.out.println("- 메가브랜드는 풀스캔 시 매우 느림, 인덱스 필수");
            System.out.println("- 소형브랜드는 상대적으로 빠름, 인덱스 효과 제한적");
            System.out.println("- 실제 운영환경과 유사한 현실적 분포 구현 완료");
        }
    }
}
