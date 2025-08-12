package com.loopers.support.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(TestDataGenerator.class);

    private static final String URL = "jdbc:mysql://localhost:3306/loopers";
    private static final String USERNAME = "application";
    private static final String PASSWORD = "application";

    @Test
    void generateTestData() {
        System.out.println("🚀 성능 테스트용 대용량 데이터 생성을 시작합니다.");
        System.out.println("📋 목적: 브랜드 필터 + 좋아요 순 정렬 성능 테스트");
        log.info("🚀 성능 테스트용 대용량 데이터 생성을 시작합니다.");

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

            // 6. 데이터 생성
            executeDataGeneration(conn);

            // 7. 수동 커밋
            conn.commit();
            System.out.println("✅ 데이터 커밋 완료!");

            // 8. 결과 확인
            verifyDataGeneration(conn);

            // 9. 성능 테스트 안내
            printPerformanceTestGuide();

            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            long durationSeconds = durationMillis / 1000;

            System.out.println("✅ 성능 테스트용 데이터 생성 완료!");
            System.out.println("   소요 시간: " + durationMillis + "ms (" + durationSeconds + "초)");
            log.info("✅ 성능 테스트용 데이터 생성 완료! 소요 시간: {}ms ({}초)", durationMillis, durationSeconds);

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

    @Test
    void checkDataStatus() {
        System.out.println("📊 현재 데이터 상태를 확인합니다.");
        log.info("📊 현재 데이터 상태를 확인합니다.");

        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            debugConnection(conn);
            verifyDataGeneration(conn);
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

            verifyDataGeneration(conn);

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
                    money DECIMAL(19,2) NOT NULL,
                    quantity INT NOT NULL,
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

            System.out.println("현재 데이터: 브랜드 " + brandCount + "개, 상품 " + productCount + "개, 좋아요 " + likeCount + "개");
            log.info("현재 데이터: 브랜드 {}개, 상품 {}개, 좋아요 {}개", brandCount, productCount, likeCount);

            if (productCount >= 50000) {
                System.out.println("⚠️ 이미 충분한 테스트 데이터가 존재합니다. (상품 " + productCount + "개)");
                log.info("⚠️ 이미 충분한 테스트 데이터가 존재합니다. (상품 {}개)", productCount);

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

            System.out.println("기존 데이터 삭제 완료: 좋아요 " + likesDeleted + "개, 상품 " + productsDeleted + "개, 브랜드 " + brandsDeleted + "개");
            log.info("기존 데이터 삭제 완료: 좋아요 {}개, 상품 {}개, 브랜드 {}개", likesDeleted, productsDeleted, brandsDeleted);
        }
    }

    private void executeDataGeneration(Connection conn) throws SQLException {
        System.out.println("📜 데이터 생성 실행 중...");
        log.info("📜 데이터 생성 실행 중...");

        // MySQL 설정
        System.out.println("1️⃣ MySQL 설정 중...");
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("SET @@cte_max_recursion_depth = 100000");
        }

        // 브랜드 생성
        System.out.println("2️⃣ 브랜드 데이터 생성 중...");
        generateBrands(conn);

        // 상품 생성 (배치 처리)
        System.out.println("3️⃣ 상품 데이터 생성 중...");
        generateProducts(conn);

        // 좋아요 생성 (배치 처리)
        System.out.println("4️⃣ 좋아요 데이터 생성 중...");
        generateLikes(conn);

        System.out.println("✅ 모든 데이터 생성 완료");
        log.info("✅ 모든 데이터 생성 완료");
    }

    private void generateBrands(Connection conn) throws SQLException {
        String sql = """
            INSERT INTO brand (name, description) VALUES
            ('삼성', '삼성에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('LG', 'LG에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('애플', '애플에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('나이키', '나이키에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('아디다스', '아디다스에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('유니클로', '유니클로에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('자라', '자라에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('H&M', 'H&M에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('무지', '무지에서 제공하는 최고 품질의 제품들을 만나보세요.'),
            ('올리브영', '올리브영에서 제공하는 최고 품질의 제품들을 만나보세요.')
            """;

        try (Statement stmt = conn.createStatement()) {
            int result = stmt.executeUpdate(sql);
            System.out.println("브랜드 생성 완료: " + result + " 행");
            log.info("브랜드 생성 완료: {} 행", result);
        }
    }

    private void generateProducts(Connection conn) throws SQLException {
        System.out.println("상품 데이터 생성 중 (배치 처리)...");

        int totalProducts = 100000;
        int batchSize = 10000;
        int batches = totalProducts / batchSize;
        int totalCreated = 0;

        try (Statement stmt = conn.createStatement()) {
            for (int batch = 0; batch < batches; batch++) {
                int startId = batch * batchSize + 1;
                int endId = (batch + 1) * batchSize;

                String sql = String.format("""
                    INSERT INTO product (brand_id, name, money, quantity, created_at)
                    WITH RECURSIVE numbers AS (
                        SELECT %d AS n
                        UNION ALL
                        SELECT n + 1 FROM numbers WHERE n < %d
                    )
                    SELECT 
                        ((n %% 10) + 1) AS brand_id,
                        CONCAT('테스트상품 ', LPAD(n, 6, '0')) AS name,
                        (1000 + (n %% 50000)) AS money,
                        (n %% 100) AS quantity,
                        DATE_SUB(NOW(), INTERVAL (n %% 365) DAY) AS created_at
                    FROM numbers
                    """, startId, endId);

                int result = stmt.executeUpdate(sql);
                totalCreated += result;

                System.out.printf("상품 배치 %d/%d 완료: %d 행 (누적: %,d 행)\n",
                    batch + 1, batches, result, totalCreated);
            }
        }

        System.out.println("상품 생성 완료: " + String.format("%,d", totalCreated) + " 행");
        log.info("상품 생성 완료: {} 행", totalCreated);
    }

    private void generateLikes(Connection conn) throws SQLException {
        System.out.println("좋아요 데이터 생성 중 (배치 처리)...");

        int totalLikes = 80000; // 중복 고려해서 목표치를 높임
        int batchSize = 5000;
        int batches = totalLikes / batchSize;
        int totalCreated = 0;

        try (Statement stmt = conn.createStatement()) {
            for (int batch = 0; batch < batches; batch++) {
                int startId = batch * batchSize + 1;
                int endId = (batch + 1) * batchSize;

                // 중복을 줄이기 위해 더 다양한 조합 생성
                String sql = String.format("""
                    INSERT IGNORE INTO likes (user_id, product_id, created_at)
                    WITH RECURSIVE numbers AS (
                        SELECT %d AS n
                        UNION ALL
                        SELECT n + 1 FROM numbers WHERE n < %d
                    )
                    SELECT 
                        CONCAT('user', LPAD(((n * 11 + %d) %% 2000) + 1, 4, '0')) AS user_id,
                        ((n * 17 + %d) %% 100000) + 1 AS product_id,
                        DATE_SUB(NOW(), INTERVAL (n %% 30) DAY) AS created_at
                    FROM numbers
                    """, startId, endId, batch * 137, batch * 239);

                int result = stmt.executeUpdate(sql);
                totalCreated += result;

                System.out.printf("좋아요 배치 %d/%d 완료: %d 행 (누적: %,d 행)\n",
                    batch + 1, batches, result, totalCreated);

                // 50,000개 달성하면 중단
                if (totalCreated >= 50000) {
                    System.out.println("🎯 목표 50,000개 달성으로 생성 중단");
                    break;
                }
            }
        }

        System.out.println("좋아요 생성 완료: " + String.format("%,d", totalCreated) + " 행");
        log.info("좋아요 생성 완료: {} 행", totalCreated);
    }

    private void verifyDataGeneration(Connection conn) throws SQLException {
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

            System.out.println("📊 현재 데이터:");
            System.out.println("   - 브랜드: " + String.format("%,d", brandCount) + "개");
            System.out.println("   - 상품: " + String.format("%,d", productCount) + "개");
            System.out.println("   - 좋아요: " + String.format("%,d", likeCount) + "개");

            log.info("📊 현재 데이터: 브랜드 {}개, 상품 {}개, 좋아요 {}개", brandCount, productCount, likeCount);

            if (productCount >= 50000) {
                System.out.println("🎯 대용량 테스트 데이터 준비 완료! 성능 테스트 가능");
                log.info("🎯 대용량 테스트 데이터 준비 완료! 성능 테스트 가능");
            }
        }
    }

    private void printPerformanceTestGuide() {
        System.out.println("\n🎯 성능 테스트 가이드:");
        System.out.println("==========================================");
        System.out.println("1️⃣ 현재 상태 테스트 (인덱스 없음):");
        System.out.println("   EXPLAIN SELECT p.* FROM product p");
        System.out.println("   LEFT JOIN likes l ON p.id = l.product_id");
        System.out.println("   WHERE p.brand_id = 1");
        System.out.println("   GROUP BY p.id ORDER BY COUNT(l.id) DESC LIMIT 20;");
        System.out.println("");
        System.out.println("2️⃣ 인덱스 생성:");
        System.out.println("   CREATE INDEX idx_product_brand_id ON product(brand_id);");
        System.out.println("   CREATE INDEX idx_likes_product_id ON likes(product_id);");
        System.out.println("");
        System.out.println("3️⃣ 개선 후 테스트:");
        System.out.println("   동일한 EXPLAIN 쿼리 실행하여 비교");
        System.out.println("==========================================\n");

        log.info("성능 테스트 가이드 출력 완료");
    }
}
