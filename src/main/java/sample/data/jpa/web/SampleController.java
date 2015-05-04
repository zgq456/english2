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
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import sample.data.jpa.domain.Article;
import sample.data.jpa.domain.Quiz;
import sample.data.jpa.domain.QuizRating;
import sample.data.jpa.domain.QuizWordBean;
import sample.data.jpa.domain.SenBean;
import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.User;
import sample.data.jpa.domain.Word;
import sample.data.jpa.service.ArticleServiceImpl;
import sample.data.jpa.service.CityService;
import sample.data.jpa.service.JqGridData;
import sample.data.jpa.service.MyConstants;
import sample.data.jpa.service.UserServiceImpl;

@Controller
@EnableScheduling
public class SampleController {

	@Autowired
	private CityService cityService;

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private ArticleServiceImpl articleService;

	// @Scheduled(fixedDelay = 2 * 60000)
	@Scheduled(cron = "0 0 * * * ?")
	public void dailyNotify() {
		System.out.println("dailyNotify invoke "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

		String accessToken = getAccessToken();

		this.articleService.sendMsg(accessToken);

	}

	private String getAccessToken() {
		String accessToken = null;
		WebDriver driver = new HtmlUnitDriver();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wxad1e1211d18cd79d&secret=4a5a2aa472e9cc890b896623c2c2facf";
		driver.get(url);
		String pageSource = driver.getPageSource();
		try {
			pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
		System.out.println("pageSource:" + pageSource);
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(pageSource);
			accessToken = (String) json.get("access_token");
			System.out.println("access_token:" + accessToken);
			System.out.println("expires_in:" + json.get("expires_in"));
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return accessToken;
	}

	@RequestMapping("/getUser")
	@ResponseBody
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

	@RequestMapping("/bindAccount")
	@ResponseBody
	public String bindAccount(String emailBind, String passwordBind) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String openId = auth.getName();
		return this.userService.bindAccount(emailBind, passwordBind, openId);
	}

	public long getUserId() {
		User user = getUser();
		return user == null ? -1 : user.getId();
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

	@RequestMapping("/updateUserReminder")
	@ResponseBody
	@Transactional(readOnly = true)
	public String updateUserReminder(String reminderVal) {
		User user = getUser();
		this.userService.updateUserReminder(user, reminderVal);
		return "";
	}

	// @RequestMapping("/getUserHeaderImg")
	// @ResponseBody
	// @Transactional(readOnly = true)
	// public String getUserHeaderImg() {
	// User user = getUser();
	// if (user == null) {
	// return "";
	// }
	// else {
	// return user.getHeadimgurl();
	// }
	// }

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

	@RequestMapping("/getArticleList2")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getArticleList2(HttpServletRequest request, int rows, int page,
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

	@RequestMapping("/getSenList2")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getSenList2(HttpServletRequest request, int rows, int page,
			String sidx, String sord, long articleId) {
		JqGridData<SenBean> gridDataList = this.articleService.getSenList2(articleId,
				getUserId(), rows, page);
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

	@RequestMapping("/getSenListForSubGrid2")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getSenListForSubGrid2(Long wordId, Long articleId) {
		JqGridData<Sentence> gridDataList = this.articleService.getSenListForSubGrid2(
				wordId, articleId, getUserId());
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

	@RequestMapping("/getWordList2")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getWordList2(HttpServletRequest request, long articleId, int rows,
			int page, String sidx, String sord) {
		JqGridData<Word> gridDataList = this.articleService.getWordList2(getUserId(),
				articleId, rows, page, sidx, sord);
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

	@RequestMapping("/deleteArticle")
	@ResponseBody
	public String deleteArticle(String id) {
		String name = null;
		String openFlag = null;
		String hideFlag = null;
		String remark = null;
		String type = null;
		String url = null;
		String oper = "del";
		String stringText = null;
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
		this.articleService.updateInterest(id, getUserId(), rank);
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
		this.articleService.updateInterest(wordId, userId, forkValue);
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

	@RequestMapping("/getRateForQuiz")
	@ResponseBody
	public double getRateForQuiz(long qId) {
		return this.articleService.getRateForQuiz(qId, getUserId());
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
	public String ignoreWord(long wordId, long days) {
		return this.articleService.ignoreWord(wordId, days, getUserId());

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
