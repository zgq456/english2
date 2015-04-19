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
import org.springframework.beans.factory.annotation.Autowired;
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
import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.User;
import sample.data.jpa.domain.Word;
import sample.data.jpa.service.ArticleServiceImpl;
import sample.data.jpa.service.CityService;
import sample.data.jpa.service.JqGridData;
import sample.data.jpa.service.UserServiceImpl;
import sample.data.jpa.weixin.WeiXinHandler;

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
		String name = auth.getName();
		User user = this.userService.getUser(name);
		return user;
	}

	public long getUserId() {
		User user = getUser();
		return user == null ? -1 : user.getId();
	}

	@RequestMapping("/weixin")
	@ResponseBody
	@Transactional(readOnly = true)
	public String weixin(HttpServletRequest request, HttpServletResponse response) {
		WeiXinHandler handler = new WeiXinHandler(request, response);
		return handler.valid();
	}

	@RequestMapping("/getUserName")
	@ResponseBody
	@Transactional(readOnly = true)
	public String getUserName() {
		User user = getUser();
		return user == null ? "Guest" : StringUtils.substring(user.getEmail(), 0, 5)
				+ "...";
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
	public Quiz getQuiz(long qId) {
		return this.articleService.getQuiz(qId);
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
			String wordArr, String senArr, String commArr) {
		String[] wordArr2 = wordArr.split("######");
		String[] senArr2 = senArr.split("######");
		String[] commArr2 = commArr.split("######");
		this.articleService.saveOrUpdateQuiz(id, name, remark, oper, wordArr2, senArr2,
				commArr2, getUserId());

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
		this.articleService.submitAnswer(answerInfo, getUserId(), qId);
		return "1";
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
	public List<QuizWordBean> getWordListForQuiz2(long id) {
		System.out.println("##invoke getWordListForQuiz  2  ##");
		List<QuizWordBean> quizWordBeanList = this.articleService.getWordListForQuiz2(id);
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
