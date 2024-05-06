package org.thingsboard.server;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class IndexController {
	@GetMapping("/index")
	public String index(String name) {
		return "ok";
	}

	@GetMapping("/sendWs")
	public void sendWs() {
	}
}
