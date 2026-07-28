package com.likelion.olion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")   // 테스트는 application-test.yaml(H2) 설정으로 실행
class OlionApplicationTests {

	@Test
	void contextLoads() {
	}

}
