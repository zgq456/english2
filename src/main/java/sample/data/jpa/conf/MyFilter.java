package sample.data.jpa.conf;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

	@Override
	@Transactional
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

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

			String code = request.getParameter("code"); // eg:
			// 021ecbe7535641f4a56b89a075df303l
			String state = request.getParameter("state"); // eg: 123

			if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(state)) {
				WeiXinUser wxUser = new WeiXinUser();
				wxUser.setOpenId("aaaaaaaaabbbbbbbbb");
				wxUser.setNickname("猴猴");
				User user = this.articleService.updateUserInDB(wxUser);
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						user.getOpenId(), user, user.getAuthorities());
				auth.setDetails(MyConstants.TERMINAL_WEIXIN);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}

			chain.doFilter(request, response);
		}

	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}