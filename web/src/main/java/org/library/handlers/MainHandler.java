package org.library.handlers;

import java.util.Map;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyFileSet.Builder;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import com.google.template.soy.tofu.SoyTofu.Renderer;
import com.wedeploy.api.sdk.ContentType;
import com.wedeploy.api.sdk.Context;
import com.wedeploy.api.sdk.Request;
import com.wedeploy.api.sdk.Response;

public class MainHandler {
	
	protected SoyFileSet getSoyFileSet(Context context) throws Exception {
		Builder builder = SoyFileSet.builder();

		context.webFiles("**/*.soy")
        .forEach(path -> builder.add(path.toFile()));

		return builder.build();
	}

	public void handle(Request request, Response response) throws Exception {
		Map<String, Object> state = request.values(Map.class);

		state.put("element", "#content > div");

		if (response.request().headers().contains("X-PJAX")) {
			response
				.contentType(ContentType.JSON)
				.header("Cache-Control", "no-cache, max-age=0, private, must-revalidate, no-store")
				.body(state)
				.end();
		}
		else {
			String content = renderSoy(
				response, "LoginForm.layout", state);

			response.contentType(ContentType.HTML).body(content).end();
		}
	}

	private String renderSoy(
			Response res, String namespace, Map<String, Object> state)
		throws Exception {

		SoyFileSet soyFileSet = getSoyFileSet(res.context());

		SoyTofu soyTofu = soyFileSet.compileToTofu();

		Renderer renderer = soyTofu.newRenderer(namespace);

		renderer.setData(state);

		return renderer.render();
	}
	
}
