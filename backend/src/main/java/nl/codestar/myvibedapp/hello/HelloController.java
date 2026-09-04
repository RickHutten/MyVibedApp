package nl.codestar.myvibedapp.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello")
class HelloController {

	@GetMapping
	HelloResponse hello() {
		return new HelloResponse("Hello, world!");
	}

	private record HelloResponse(String message) {
	}
}
