package sample.data.jpa.conf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sample.data.jpa.domain.User;
import sample.data.jpa.service.ArticleServiceImpl;
import sample.data.jpa.service.MyConstants;
import sample.data.jpa.weixin.WeiXinUser;

@Component
public class MyFilter implements Filter {
	@Autowired
	private ArticleServiceImpl articleService;

	public static String basePath = "";

	@Override
	@Transactional
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));

		if (StringUtils.isEmpty(MyFilter.basePath)) {
			String path = ((HttpServletRequest) request).getContextPath();
			int serverPort = request.getServerPort();
			String basePath = request.getScheme() + "://" + request.getServerName()
					+ (serverPort == 80 ? "" : (":" + serverPort)) + path + "/";
			MyFilter.basePath = basePath;
		}

		System.out.println(new Date() + " remoteAddr: " + request.getRemoteAddr());
		// System.out.println(new Date() + " contextPath: "
		// + ((HttpServletRequest) request).getContextPath());
		String uri = ((HttpServletRequest) request).getRequestURI();
		if (uri.endsWith(".css") || uri.endsWith(".js")) {
			chain.doFilter(request, response);
		}
		else {
			System.out.println("uri:" + uri);
			Map map = request.getParameterMap();
			Set keSet = map.entrySet();
			if (!keSet.isEmpty()) {
				System.out.println("parameters:");
			}
			for (Iterator itr = keSet.iterator(); itr.hasNext();) {
				Map.Entry me = (Map.Entry) itr.next();
				Object ok = me.getKey();
				Object ov = me.getValue();
				String[] value = new String[1];
				if (ov instanceof String[]) {
					value = (String[]) ov;
				}
				else {
					value[0] = ov.toString();
				}

				for (int k = 0; k < value.length; ++k) {
					System.out.println(ok + "=" + value[k]);
				}
			}

			boolean isDebug = false; // FIXME
			String code = request.getParameter("code"); // eg:
			// 021ecbe7535641f4a56b89a075df303l
			String state = request.getParameter("state"); // eg: 123

			if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(state)) {
				WeiXinUser wxUser = null;
				if (!isDebug) {
					System.out.println("code:" + code + " state:" + state);
					String appId = "wxad1e1211d18cd79d";
					String secret = "4a5a2aa472e9cc890b896623c2c2facf";

					// 第二步：通过code换取网页授权access_token
					String[] tokenInfo = getAccessToken(code, appId, secret);
					String accessToken = tokenInfo[0];
					String openId = tokenInfo[1];

					if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(openId)) {
						((HttpServletResponse) response).sendRedirect("ajax/ajax.html");
						// wxUser = null;
						return;
					}
					else {
						wxUser = getWeiXinUser(accessToken, openId);
					}

				}
				else {
					wxUser = new WeiXinUser();
					wxUser.setOpenId("aaaaaaaaabbbbbbbbbc");
					wxUser.setNickname("猴猴2");

				}

				if (wxUser != null) {
					User user = this.articleService.updateUserInDB(wxUser);
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
							user.getOpenId(), user, user.getAuthorities());
					auth.setDetails(MyConstants.TERMINAL_WEIXIN);
					// SecurityContextHolder.getContext().setAuthentication(auth);
					SecurityContext securityContext = SecurityContextHolder.getContext();
					securityContext.setAuthentication(auth);
					HttpSession session = ((HttpServletRequest) request).getSession();
					session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
				}
			}

			chain.doFilter(request, response);
		}

	}

	/**
	 * @param accessToken
	 * @param openId
	 * @return
	 */
	private WeiXinUser getWeiXinUser(String accessToken, String openId) {
		String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken
				+ "&openid=" + openId + "&lang=zh_CN";
		System.out.println("getWeiXinUser url:" + url);
		WebDriver driver = new HtmlUnitDriver();
		driver.get(url);
		String pageSource = driver.getPageSource();
		try {
			pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("pageSource:" + pageSource);

		// JsonElement jsonElm = new JsonParser().parse(pageSource);
		JSONObject json = null;
		try {
			json = (JSONObject) new JSONParser().parse(pageSource);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}

		WeiXinUser wxUser = new WeiXinUser();
		wxUser.setOpenId((String) json.get("openid"));
		wxUser.setNickname((String) json.get("nickname"));
		wxUser.setSex(((Long) json.get("sex")).intValue());
		wxUser.setProvince((String) json.get("province"));
		wxUser.setCity((String) json.get("city"));
		wxUser.setCountry((String) json.get("country"));
		wxUser.setHeadimgurl((String) json.get("headimgurl"));
		System.out.println("wxUser:" + wxUser);
		return wxUser;
	}

	private String[] getAccessToken(String code, String appId, String secret) {
		String getAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
				+ appId
				+ "&secret="
				+ secret
				+ "&code="
				+ code
				+ "&grant_type=authorization_code";

		WebDriver driver = new HtmlUnitDriver();
		driver.get(getAccessTokenUrl);
		String pageSource = driver.getPageSource();
		try {
			pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("pageSource:" + pageSource);
		JSONObject json = null;
		try {
			json = (JSONObject) new JSONParser().parse(pageSource);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		String accessToken = (String) json.get("access_token");
		String refreshToken = (String) json.get("refresh_token");
		String openId = (String) json.get("openid");
		System.out.println("accessToken:" + accessToken);
		System.out.println("refreshToken:" + refreshToken);
		System.out.println("openid:" + openId);
		return new String[] { accessToken, openId };
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}