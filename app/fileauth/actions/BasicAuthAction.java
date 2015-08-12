package fileauth.actions;

import models.Account;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;

import com.ning.http.util.Base64;
import com.typesafe.config.ConfigFactory;

public class BasicAuthAction extends Action<Object> {

	private static final String AUTHORIZATION = "authorization";
	private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	private static final String REALM = getRealm();

	public F.Promise<Result> call(Http.Context context) throws Throwable {

		String authHeader = context.request().getHeader(AUTHORIZATION);
		if (authHeader == null) {
			return sendAuthRequest(context);
		}

		String auth = authHeader.substring(6);

		byte[] decodedAuth = Base64.decode(auth);
		String[] credString = new String(decodedAuth, "UTF-8").split(":");

		if (credString == null || credString.length != 2) {
			return sendAuthRequest(context);
		}

		String username = credString[0];
		String password = credString[1];
		Account authUser = Account.authenticate(username, password);
		if (authUser == null) {
			return sendAuthRequest(context);
		}
		context.request().setUsername(username);
		return delegate.call(context);
	}

	private F.Promise<Result> sendAuthRequest(Context context) {
		context.response().setHeader(WWW_AUTHENTICATE, REALM);
		return F.Promise.pure((Result) unauthorized());
	}

	private static String getRealm() {
		if (ConfigFactory.load().hasPath("application.name")) {
			return "Basic realm=\""
					+ ConfigFactory.load().getString("application.name") + "\"";
		}
		return "Basic realm=\"NA\"";
	}
}