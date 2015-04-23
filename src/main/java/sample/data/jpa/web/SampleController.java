/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.data.jpa.web;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import sample.data.jpa.domain.Article;
import sample.data.jpa.domain.Quiz;
import sample.data.jpa.domain.QuizRating;
import sample.data.jpa.domain.QuizWordBean;
import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.User;
import sample.data.jpa.domain.Word;
import sample.data.jpa.service.ArticleServiceImpl;
import sample.data.jpa.service.CityService;
import sample.data.jpa.service.JqGridData;
import sample.data.jpa.service.MyConstants;
import sample.data.jpa.service.UserServiceImpl;
import sample.data.jpa.weixin.WeiXinUser;

@Controller
public class SampleController {

	@Autowired
	private CityService cityService;

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private ArticleServiceImpl articleService;

	public User getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (((Integer) auth.getDetails()) == MyConstants.TERMINAL_WEB) {
			String email = auth.getName();
			user = this.userService.getUserByEmail(email);
		}
		else {
			String openId = auth.getName();
			user = this.userService.getUserByOpenId(openId);
		}
		return user;
	}

	public long getUserId() {
		User user = getUser();
		return user == null ? -1 : user.getId();
	}

	@RequestMapping("/weixinLogin")
	public ModelAndView weixinLogin(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		WeiXinUser wxUser = null;
		boolean isDebug = true; // FIXME

		if (!isDebug) {
			String code = request.getParameter("code"); // eg:
			// 021ecbe7535641f4a56b89a075df303l
			String state = request.getParameter("state"); // eg: 123

			System.out.println("code:" + code + " state:" + state);
			String appId = "wxad1e1211d18cd79d";
			String secret = "4a5a2aa472e9cc890b896623c2c2facf";

			// 第二步：通过code换取网页授权access_token
			String[] tokenInfo = getAccessToken(code, appId, secret);
			String accessToken = tokenInfo[0];
			String openId = tokenInfo[1];

			wxUser = getWeiXinUser(accessToken, openId);
		}
		else {
			wxUser = new WeiXinUser();
			wxUser.setOpenId("aaaaaaaaabbbbbbbbb");
			wxUser.setNickname("猴猴");
		}

		User user = this.articleService.updateUserInDB(wxUser);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
				user.getOpenId(), user, user.getAuthorities());
		auth.setDetails(MyConstants.TERMINAL_WEIXIN);
		SecurityContextHolder.getContext().setAuthentication(auth);
		return new ModelAndView("redirect:/html/ajax/ajax.html#page/articles");
	}

	/**
	 * @param accessToken
	 * @param openId
	 * @return
	 */
	private WeiXinUser getWeiXinUser(String accessToken, String openId) throws Exception {
		String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken
				+ "&openid=" + openId + "&lang=zh_CN";
		System.out.println("getWeiXinUser url:" + url);
		WebDriver driver = new HtmlUnitDriver();
		driver.get(url);
		String pageSource = driver.getPageSource();
		pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		System.out.println("pageSource:" + pageSource);

		// JsonElement jsonElm = new JsonParser().parse(pageSource);
		JSONObject json = (JSONObject) new JSONParser().parse(pageSource);

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

	private String[] getAccessToken(String code, String appId, String secret)
			throws Exception {
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
		pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		System.out.println("pageSource:" + pageSource);
		JSONObject json = (JSONObject) new JSONParser().parse(pageSource);
		String accessToken = (String) json.get("access_token");
		String refreshToken = (String) json.get("refresh_token");
		String openId = (String) json.get("openid");
		System.out.println("accessToken:" + accessToken);
		System.out.println("refreshToken:" + refreshToken);
		System.out.println("openid:" + openId);
		return new String[] { accessToken, openId };
	}

	@RequestMapping("/getUserName")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getUserName() {
		User user = getUser();
		if (user == null) {
			return "Guest";
		}
		else {
			if (StringUtils.isEmpty(user.getNickname())) {
				return StringUtils.substring(user.getEmail(), 0, 5) + "...";
			}
			else {
				return user.getNickname();
			}
		}
	}

	@RequestMapping("/")
	@ResponseBody
	@Transactional(readOnly = true)
	public String helloWorld(HttpServletRequest request) {
		System.out.println("helloword");
		// return this.cityService.getCity("Bath", "UK").getName();
		return "test2 " + new Date().toString() + " user:"
				+ request.getSession().getAttribute("userName");
	}

	@RequestMapping("/getArticleList")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getArticleList(HttpServletRequest request, int rows, int page,
			String sidx, String sord) {
		JqGridData<Article> gridDataList = this.articleService.getArticleList(
				getUserId(), rows, page, sidx, sord);
		return gridDataList.getJsonString();
	}

	@RequestMapping("/getQuizList")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getQuizList(HttpServletRequest request, int rows, int page,
			String sidx, String sord) {
		JqGridData<Quiz> gridDataList = this.articleService.getQuizList(rows, page, sidx,
				sord);
		return gridDataList.getJsonString();
	}

	@RequestMapping("/getQuiz")
	@ResponseBody
	@Transactional(readOnly = true)
	public Quiz getQuiz(long qId, String password) {
		return this.articleService.getQuiz(qId, password);
	}

	@RequestMapping("/enhanceWord")
	public String enhanceWord() {
		this.articleService.enhanceWord();
		return "done";
	}

	@RequestMapping("/getSenList")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getSenList(HttpServletRequest request, int rows, int page, String sidx,
			String sord) {
		JqGridData<SenSummary> gridDataList = this.articleService.getSenList(getUserId(),
				rows, page, sidx, sord);
		return gridDataList.getJsonString();
	}

	@RequestMapping("/getSenListForSubGrid")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getSenListForSubGrid(Long wordId) {
		JqGridData<Sentence> gridDataList = this.articleService.getSenListForSubGrid(
				wordId, getUserId());
		return gridDataList.getJsonString();
	}

	@RequestMapping("/getWordList")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getWordList(HttpServletRequest request, int rows, int page,
			String sidx, String sord) {
		JqGridData<Word> gridDataList = this.articleService.getWordList(getUserId(),
				rows, page, sidx, sord);
		return gridDataList.getJsonString();
	}

	// @RequestParam("file") MultipartFile file,

	@RequestMapping("/addOrUpdateArticle")
	@ResponseBody
	public String addOrUpdateArticle(String id, String name, String openFlag,
			String hideFlag, String remark, String type, String url, String oper,
			String stringText) {
		Article article = new Article();
		article.setName(name);
		if ("on".equals(openFlag)) {
			openFlag = "Yes";
		}
		else if ("off".equals(openFlag)) {
			openFlag = "No";
		}
		// hideFlag = "on".equals(hideFlag) ? "Yes" : "No";
		article.setOpenFlag(openFlag);
		article.setHideFlag(hideFlag);
		article.setRemark(remark);
		article.setType(type);
		article.setUrl(url);
		this.articleService.saveOrUpdate(id, article, oper, getUserId(), stringText);
		return "";
	}

	@RequestMapping("/addOrUpdateQuiz")
	@ResponseBody
	public String addOrUpdateQuiz(String id, String name, String remark, String oper,
			String wordArr, String senArr, String commArr, String password) {
		String[] wordArr2 = wordArr.split("######");
		String[] senArr2 = senArr.split("######");
		String[] commArr2 = commArr.split("######");
		this.articleService.saveOrUpdateQuiz(id, name, remark, oper, wordArr2, senArr2,
				commArr2, getUserId(), password);

		// Article article = new Article();
		// article.setName(name);
		// openFlag = "on".equals(openFlag) ? "Yes" : "No";
		// article.setOpenFlag(openFlag);
		// article.setRemark(remark);
		// article.setType(type);
		// article.setUrl(url);
		// this.articleService.saveOrUpdate(id, article, oper, getUserId(), stringText);
		return "";
	}

	@RequestMapping("/addOrUpdateSen")
	@ResponseBody
	public String addOrUpdateSen(String id,
			@RequestParam(value = "sen.content") String content, String usefulFlag,
			String oper) {
		this.articleService.saveOrUpdateSen(id, content, usefulFlag, oper, getUserId());
		return "";
	}

	@RequestMapping("/updateRank")
	@ResponseBody
	public String updateRank(Long id, int rank) {
		this.articleService.updateRank(id, getUserId(), rank);
		return "1";
	}

	@RequestMapping("/submitAnswer")
	@ResponseBody
	public String submitAnswer(String answerInfo, long qId) {
		return this.articleService.submitAnswer(answerInfo, getUserId(), qId);
	}

	@RequestMapping("/toggleWordFork")
	@ResponseBody
	public String toggleWordFork(long wordId, int forkValue) {
		long userId = getUserId();
		if (userId == 0) {
			return "0";
		}
		this.articleService.updateRank(wordId, userId, forkValue);
		return "1";
	}

	@RequestMapping("/toggleForkSen")
	@ResponseBody
	public String toggleForkSen(long senId, int forkValue) {
		long userId = getUserId();
		if (userId == 0) {
			return "0";
		}
		this.articleService.toggleMarkSen2(senId, userId, forkValue);
		return "1";
	}

	@RequestMapping("/toggleMarkSen")
	@ResponseBody
	public String toggleMarkSen(Long id) {
		return this.articleService.toggleMarkSen(id, getUserId());
	}

	@RequestMapping("/myregister")
	@ResponseBody
	public String register(String username, String password) {
		System.out.println("###register###");
		return this.articleService.register(username, password);
	}

	@RequestMapping("/uploadFile")
	@ResponseBody
	public String uploadFile(@RequestParam("file") MultipartFile file) {
		System.out.println("file:" + file);
		if (!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();
				System.out.println("bytes:" + bytes);
				File destFile = new File("uploadFiles/" + getUserId() + "/"
						+ file.getOriginalFilename());
				System.out.println("destFile:" + destFile.getAbsolutePath());
				FileUtils.writeByteArrayToFile(destFile, bytes);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				throw new UnsupportedOperationException("Auto-generated method stub", e);
			}
			// store the bytes somewhere
			// åœ¨è¿™é‡Œå°±å¯ä»¥å¯¹fileè¿›è¡Œå¤„ç†äº†ï¼Œå¯ä»¥æ&nbsp;¹æ®è‡ªå·±çš„éœ€æ±‚æŠŠå®ƒå­˜åˆ°æ•°æ®åº“æˆ–è€…æœåŠ¡å™¨çš„æŸä¸ªæ–‡ä»¶å¤¹

		}
		else {
		}
		return "";
	}

	@RequestMapping("/getWordListForQuiz")
	@ResponseBody
	public List<QuizWordBean> getWordListForQuiz() { // this is for free organized quiz
		System.out.println("##invoke getWordListForQuiz##");
		List<QuizWordBean> quizWordBeanList = this.articleService
				.getWordListForQuiz(getUserId());
		return quizWordBeanList;
	}

	@RequestMapping("/getRateListForQuiz")
	@ResponseBody
	public List<QuizRating> getRateListForQuiz(long qId) {
		System.out.println("##invoke getRateListForQuiz##");
		return this.articleService.getRateListForQuiz(qId);
	}

	@RequestMapping("/getWordListForQuiz2")
	@ResponseBody
	public List<QuizWordBean> getWordListForQuiz2(long id, String password) {
		System.out.println("##invoke getWordListForQuiz  2  ##");
		List<QuizWordBean> quizWordBeanList = this.articleService.getWordListForQuiz2(id,
				password);
		return quizWordBeanList;
	}

	@RequestMapping("/getWordExplain")
	@ResponseBody
	public String getWordExplain(String wordValue) {
		return this.articleService.getWordExplain(wordValue);
	}

	@RequestMapping("/saveWordExplain")
	@ResponseBody
	public String saveWordExplain(String wordValue, String explainValue) {
		return this.articleService.saveWordExplain(wordValue, explainValue);

	}

	@RequestMapping("/ignoreWord")
	@ResponseBody
	public String ignoreWord(long wordId) {
		return this.articleService.ignoreWord(wordId, getUserId());

	}
	// String name = "test_" + new Date() + ".txt";
	// if (!file.isEmpty()) {
	// try {
	// byte[] bytes = file.getBytes();
	// BufferedOutputStream stream = new BufferedOutputStream(
	// new FileOutputStream(new File(name)));
	// stream.write(bytes);
	// stream.close();
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// @RequestMapping("/login")
	// @ResponseBody
	// @Transactional(readOnly = true)
	// public String login(HttpServletRequest request, String email, String password) {
	// System.out.println("login email:" + email + " password:" + password);
	// System.out.println("request:" + request);
	// User user = this.userService.getUser(email, password);
	// String userName = StringUtils.isEmpty(user.getName()) ? user.getEmail() : user
	// .getName();
	// request.getSession().setAttribute("userName", userName);
	// return userName;
	// }
}
