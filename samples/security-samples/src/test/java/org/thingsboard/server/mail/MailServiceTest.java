package org.thingsboard.server.mail;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thingsboard.common.util.JacksonUtil;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */

@SpringBootTest
class MailServiceTest {
	@Autowired
	private MailService mailService;


	@Test
	void test() {
		JsonNode jsonConfig = JacksonUtil.newArrayNode();
//		mailService.sendTestMail();
	}

}
