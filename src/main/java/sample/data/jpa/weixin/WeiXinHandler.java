package sample.data.jpa.weixin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class WeiXinHandler {
	final String TOKEN = "weixin";
	HttpServletRequest final_request = null;
	HttpServletResponse final_response = null;

	public WeiXinHandler(HttpServletRequest request, HttpServletResponse response) {
		this.final_request = request;
		this.final_response = response;
	}

	public String valid() {
		System.out.println(new Date() + " valid");
		String echostr = this.final_request.getParameter("echostr");
		if ((echostr == null) || (echostr.isEmpty())) {
			return responseMsg();
		}
		else {
			System.out.println(new Date() + " echostr:" + echostr);
			if (checkSignature()) {
				// print(echostr);
				return (echostr);
			}
			else {
				return ("error");
			}
		}
	}

	public String responseMsg() {
		String postStr = null;
		try {
			postStr = readStreamParameter(this.final_request.getInputStream());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("postStr:" + postStr);
		if ((postStr != null) && (!(postStr.isEmpty()))) {
			String msgType;
			String contentStr;
			String resultStr;
			Document document = null;
			try {
				document = DocumentHelper.parseText(postStr);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			if (document == null) {
				// print("");
				// return;
				return "";
			}
			Element root = document.getRootElement();
			String fromUsername = root.elementText("FromUserName");
			String toUsername = root.elementText("ToUserName");
			String keyword = root.elementTextTrim("Content");
			String time = new Date().getTime() + "";
			String textTemplate = "<xml><ToUserName><![CDATA[%1$s]]></ToUserName><FromUserName><![CDATA[%2$s]]></FromUserName><CreateTime>%3$s</CreateTime><MsgType><![CDATA[%4$s]]></MsgType><Content><![CDATA[%5$s]]></Content><FuncFlag>0</FuncFlag></xml>";

			String newsTemplate = "<xml><ToUserName><![CDATA[%1$s]]></ToUserName><FromUserName><![CDATA[%2$s]]></FromUserName><CreateTime>%3$s</CreateTime><MsgType><![CDATA[%4$s]]></MsgType><ArticleCount><![CDATA[%5$s]]></ArticleCount><Articles>\t<item>\t\t<Title><![CDATA[%6$s]]></Title>\t\t<Description><![CDATA[%7$s]]></Description>\t\t<Url><![CDATA[%8$s]]></Url>\t</item></Articles><FuncFlag>0</FuncFlag></xml>";

			String template = textTemplate;
			String menuStr = "请发送数字进行相关操作:\n1.常用信息 \n2.资源共享 \n3.微社区\n4.问题收集\n";
			if ((keyword != null) && (!(keyword.equals("")))) {
				keyword = StringUtils.trim(keyword);
				msgType = "text";
				contentStr = "欢迎关注亿丰蔚蓝郡公众号，刚刚您发送了：" + keyword;
				if ("1".equals(keyword)) {
					contentStr = "我是1";
				}
				else if ("2".equals(keyword)) {
					template = newsTemplate;
					// contentStr = "请发送查询+关键字，例如:查询物业电话";
				}
				else if ("3".equals(keyword)) {
					template = newsTemplate;
				}
				else if ("4".equals(keyword)) {
					template = newsTemplate;
				}
				else if (StringUtils.startsWith(keyword, "查询")) {
					String queryKeyword = keyword.substring(2);
					contentStr = "您想查询:" + queryKeyword + "\n";
				}
				else if ("m".equalsIgnoreCase(keyword)
						|| "menu".equalsIgnoreCase(keyword)
						|| "h".equalsIgnoreCase(keyword)
						|| "help".equalsIgnoreCase(keyword) || "菜单".equals(keyword)
						|| "帮助".equals(keyword)) {
					contentStr = menuStr;
				}
				else {
					contentStr = "暂不支持该输入, " + menuStr;
				}
				resultStr = null;
				if (template.equals(textTemplate)) {
					resultStr = String.format(template, new Object[] { fromUsername,
							toUsername, time, msgType, contentStr });
				}
				else if ((template.equals(newsTemplate))) {
					msgType = "news";
					if ("2".equals(keyword)) {
						String addResUrl = "http://wlj.sturgeon.mopaas.com/listResource.jsp?fromUserName="
								+ fromUsername;
						// String addResUrl =
						// "http://60.186.211.215/wlj/listResource.jsp?fromUserName=" +
						// fromUsername;
						resultStr = String.format(template, new Object[] { fromUsername,
								toUsername, time, msgType, Integer.valueOf(1), "资源信息",
								"蔚蓝郡业主资源信息共享", addResUrl });
						System.out.println("addResUrl :" + addResUrl);
					}
					else if ("3".equals(keyword)) {
						// FIXME
						// resultStr = String.format(template, new Object[] {
						// fromUsername, toUsername, time, msgType,
						// Integer.valueOf(1), "向蔚蓝郡物业吐槽",
						// "给中都物业谏言献策提意见，让我们一起携手让生活更美好",
						// "http://wlj.sturgeon.mopaas.com/add.jsp?dest=wy" });
						resultStr = String.format(template, new Object[] { fromUsername,
								toUsername, time, msgType, Integer.valueOf(1), "蔚蓝郡微社区",
								"蔚蓝郡业主交流平台，包含但不限于吐槽、讨论业委会事宜、不文明现象监督等",
								"http://wx.wsq.qq.com/255558243" });
					}
					else if ("4".equals(keyword)) {
						String addCQUrl = "http://wlj.sturgeon.mopaas.com/listCQ.jsp?fromUserName="
								+ fromUsername;
						// String addResUrl =
						// "http://60.186.211.215/wlj/listResource.jsp?fromUserName=" +
						// fromUsername;
						resultStr = String.format(template, new Object[] { fromUsername,
								toUsername, time, msgType, Integer.valueOf(1), "问题信息",
								"蔚蓝郡业主问题信息收集，楼长们会定期和物业开会讨论您所提的问题", addCQUrl });
						System.out.println("addCQUrl :" + addCQUrl);
					}
				}
				System.out.println(new Date() + " resultStr:" + resultStr);
				// print(resultStr);
				return (resultStr);
			}
			else {
				msgType = "text";
				contentStr = "欢迎关注亿丰蔚蓝郡, " + menuStr;
				resultStr = String.format(textTemplate, new Object[] { fromUsername,
						toUsername, time, msgType, contentStr });
				// print(resultStr);
				return (resultStr);
			}
		}
		else {
			// print("");
			return "";
		}
	}

	public boolean checkSignature() {
		String signature = this.final_request.getParameter("signature");
		String timestamp = this.final_request.getParameter("timestamp");
		String nonce = this.final_request.getParameter("nonce");
		String token = "weixin";
		String[] tmpArr = { token, timestamp, nonce };
		Arrays.sort(tmpArr);
		String tmpStr = ArrayToString(tmpArr);
		tmpStr = SHA1Encode(tmpStr);

		return (!(tmpStr.equalsIgnoreCase(signature)));
	}

	// public void print(String content) {
	// try {
	// this.final_response.getWriter().print(content);
	// this.final_response.getWriter().flush();
	// this.final_response.getWriter().close();
	// }
	// catch (Exception localException) {
	// }
	// }

	public String ArrayToString(String[] arr) {
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			bf.append(arr[i]);
		}
		return bf.toString();
	}

	public String SHA1Encode(String sourceString) {
		String resultString = null;
		try {
			resultString = new String(sourceString);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			resultString = byte2hexString(md.digest(resultString.getBytes()));
		}
		catch (Exception localException) {
		}
		return resultString;
	}

	public final String byte2hexString(byte[] bytes) {
		StringBuffer buf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; ++i) {
			if ((bytes[i] & 0xFF) < 16) {
				buf.append("0");
			}
			buf.append(Long.toString(bytes[i] & 0xFF, 16));
		}
		return buf.toString().toUpperCase();
	}

	public String readStreamParameter(ServletInputStream in) {
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer.toString();
	}
}