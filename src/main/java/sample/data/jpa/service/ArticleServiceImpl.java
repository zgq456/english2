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

package sample.data.jpa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sample.data.jpa.conf.MyFilter;
import sample.data.jpa.conf.MySettings;
import sample.data.jpa.domain.Article;
import sample.data.jpa.domain.ArticleBean;
import sample.data.jpa.domain.ArticleWordAsso;
import sample.data.jpa.domain.Audio;
import sample.data.jpa.domain.AudioAnswer;
import sample.data.jpa.domain.AudioSnippet;
import sample.data.jpa.domain.MyWordSummary;
import sample.data.jpa.domain.Quiz;
import sample.data.jpa.domain.QuizContent;
import sample.data.jpa.domain.QuizRating;
import sample.data.jpa.domain.QuizWordBean;
import sample.data.jpa.domain.SenBean;
import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.User;
import sample.data.jpa.domain.UserArticleForkAsso;
import sample.data.jpa.domain.UserSenAsso;
import sample.data.jpa.domain.UserWordAsso;
import sample.data.jpa.domain.Word;
import sample.data.jpa.domain.WordBean2;
import sample.data.jpa.repository.ArticleRepository;
import sample.data.jpa.repository.ArticleWordAssoRepository;
import sample.data.jpa.repository.AudioAnswerRepository;
import sample.data.jpa.repository.AudioRepository;
import sample.data.jpa.repository.AudioSnippetRepository;
import sample.data.jpa.repository.QuizContentRepository;
import sample.data.jpa.repository.QuizRepository;
import sample.data.jpa.repository.QuizResultRepository;
import sample.data.jpa.repository.SentenceRepository;
import sample.data.jpa.repository.UserArticleForkAssoRepository;
import sample.data.jpa.repository.UserRepository;
import sample.data.jpa.repository.UserSenAssoRepository;
import sample.data.jpa.repository.UserWordAssoRepository;
import sample.data.jpa.repository.WordRepository;
import sample.data.jpa.service.util.MyUtil;
import sample.data.jpa.weixin.Sign;
import sample.data.jpa.weixin.WeiXinUser;

import com.google.code.mp3fenge.Mp3Fenge;

import edu.stanford.nlp.process.DocumentPreprocessor;

@Component("articleService")
@Transactional
public class ArticleServiceImpl {

	private final ArticleRepository articleRepository;
	private final ArticleWordAssoRepository articleWordAssoRepository;
	private final WordRepository wordRepository;
	private final SentenceRepository sentenceRepository;
	private final UserRepository userRepository;
	private final UserSenAssoRepository userSenAssoRepository;
	private final UserWordAssoRepository userWordAssoRepository;
	@Autowired
	private Md5PasswordEncoder passwordEncoder;

	@Autowired
	UserArticleForkAssoRepository userArticleForkAssoRepository;
	@Autowired
	QuizRepository quizRepository;
	@Autowired
	QuizContentRepository quizContentRepository;
	@Autowired
	AudioSnippetRepository audioSnippetRepository;
	@Autowired
	AudioAnswerRepository audioAnswerRepository;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	QuizResultRepository quizResultRepository;

	@Autowired
	AudioRepository audioRepository;

	@Autowired
	private MySettings mySettings;

	@Autowired
	public ArticleServiceImpl(ArticleRepository articleRepository,
			ArticleWordAssoRepository articleWordAssoRepository,
			WordRepository wordRepository, SentenceRepository sentenceRepository,
			UserRepository userRepository, UserSenAssoRepository userSenAssoRepository,
			UserWordAssoRepository userWordAssoRepository) {
		this.articleRepository = articleRepository;
		this.articleWordAssoRepository = articleWordAssoRepository;
		this.wordRepository = wordRepository;
		this.sentenceRepository = sentenceRepository;
		this.userRepository = userRepository;
		this.userSenAssoRepository = userSenAssoRepository;
		this.userWordAssoRepository = userWordAssoRepository;
	}

	@Bean
	public Md5PasswordEncoder passwordEncoder() {
		return new Md5PasswordEncoder();
	}

	/**
	 * get for custom quiz
	 * @param id
	 * @return
	 */
	public List<QuizWordBean> getWordListForQuiz2(long quizId, String password) {
		Quiz quiz = this.quizRepository.findOne(quizId);
		if (StringUtils.isNotEmpty(quiz.getPassword())) {
			if (!quiz.getPassword().equals(
					this.passwordEncoder.encodePassword(password, null))) {
				return new ArrayList<QuizWordBean>();
			}
		}
		List<QuizContent> quizContentList = this.quizContentRepository
				.findByQuizId(quizId);
		List<QuizWordBean> results = new ArrayList<QuizWordBean>();
		for (int i = 0; i < quizContentList.size(); i++) {
			QuizContent qc = quizContentList.get(i);
			QuizWordBean qwb = new QuizWordBean(qc.getWord().getId(), qc.getWord()
					.getValue(), qc.getWord().getExplain2(), 5, qc.getSentence()
					.getContent(), qc.getWord().getMark(), qc.getComment());
			qwb.setSenId(qc.getSentence().getId());
			results.add(qwb);
		}

		long userId = 0L;
		AtomicInteger startQuesIndex = new AtomicInteger();
		startQuesIndex.set(-1);
		generateQuestion(userId, results, MyConstants.CUSTOM_QUIZ, startQuesIndex, null);
		return results;
	}

	public List<QuizWordBean> getWordListFromQuizResult(long userId, Set<Long> wordIdSet,
			AtomicInteger startQuesIndex) {
		String sql = "SELECT qr.word_id, w.value, w.explain2, 5 rank, w.mark, sum(qr.is_right) right_count, max(qr.last_upt) last_upt"
				+ " FROM quiz_result qr, word w where qr.word_id = w.id and qr.user_id = ? "
				+ " group by qr.word_id having  sum(qr.is_right) < 7 order by right_count asc ";
		// System.out.println("begin getWordListFromQuizResult " + new Date());
		List<QuizWordBean> results2 = new ArrayList<QuizWordBean>();
		List<QuizWordBean> results = this.jdbcTemplate.query(sql,
				new Object[] { userId }, new RowMapper<QuizWordBean>() {
					@Override
					public QuizWordBean mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return new QuizWordBean(rs.getLong("word_id"), rs
								.getString("value"), rs.getString("explain2"), rs
								.getInt("rank"), "", rs.getInt("mark"), "", rs
								.getInt("right_count"), rs.getString("last_upt"));
					}
				});

		int[] reviewDaysArr = { 0, 1, 2, 4, 7, 11, 16, 22 };
		for (int i = 0; i < results.size(); i++) {
			QuizWordBean qwb = results.get(i);
			wordIdSet.add(qwb.getWordId());
			int rightCount = qwb.getRightCount();
			String lastUptStr = qwb.getLastUpt();
			Date lastUpt = null;
			try {
				lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastUptStr);
			}
			catch (ParseException e) {
				e.printStackTrace();
				lastUpt = new Date();
			}

			Calendar currCal = Calendar.getInstance();
			Calendar lastUptCal = Calendar.getInstance();
			lastUptCal.setTime(lastUpt);
			lastUptCal.add(Calendar.DATE, reviewDaysArr[rightCount]);
			if (lastUptCal.before(currCal)) {
				results2.add(qwb);
			}
		}

		// System.out.println("begin generateQuestion " + new Date());
		generateQuestion(userId, results2, MyConstants.FREE_QUIZ, startQuesIndex, null);

		// return results.size() > 10 ? results.subList(0, 10) : results; // FIXME
		return results2; // FIXME
	}

	/**
	 * get word list for free quiz
	 * @param userId
	 * @return
	 */
	public List<QuizWordBean> getWordListForQuiz(long userId) {
		System.out.println("###begin getWordListForQuiz " + new Date());
		int pageSize = 10;
		AtomicInteger startQuesIndex = new AtomicInteger();
		startQuesIndex.set(-1);
		List<QuizWordBean> results = new ArrayList<QuizWordBean>();
		Set<Long> wordIdSet = new HashSet<Long>();
		List<QuizWordBean> resultsFromQR = getWordListFromQuizResult(userId, wordIdSet,
				startQuesIndex);
		results.addAll(resultsFromQR);

		System.out.println("###begin get data from UWA " + new Date());
		// generateQuestion(userId, results, MyConstants.FREE_QUIZ, startQuesIndex);

		int startIndex = 0;
		do {
			if (results.size() < pageSize) {
				// String sql =
				// "select w.id word_id, w.value, w.explain2, w.mark, uwa.rank, count(*) myhit from"
				// +
				// " user u, user_article_fork_asso uafa, article_word_asso awa, word w, user_word_asso uwa "
				// +
				// " where u.id = uafa.user_id and uwa.word_id = w.id  and u.id = uwa.user_id and u.id = ? and uwa.interest = 1 "
				// +
				// " and w.id not in (select distinct word_id from quiz_result where user_id = ?) "
				// +
				// " and awa.article_id = uafa.article_id and w.id = awa.word_id group by w.id order by rank desc,  myhit desc"
				// + " limit " + startIndex + ", 50";
				String sql = "select w.id word_id, w.value, w.explain2, w.mark, uwa.rank, count(*) myhit from"
						+ " user u, article_word_asso awa, word w, user_word_asso uwa "
						+ " where uwa.word_id = w.id  and u.id = uwa.user_id and u.id = ? and uwa.interest = 1 "
						+ " and w.id not in (select distinct word_id from quiz_result where user_id = ?) "
						+ " and w.id = awa.word_id group by w.id order by rank desc,  myhit desc"
						+ " limit " + startIndex + ", 50";
				startIndex += 50;
				// System.out.println("begin getWordListForQuiz " + new Date());
				List<QuizWordBean> resultsFromUWA = this.jdbcTemplate.query(sql,
						new Object[] { userId, userId }, new RowMapper<QuizWordBean>() {
							@Override
							public QuizWordBean mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								return new QuizWordBean(rs.getLong("word_id"), rs
										.getString("value"), rs.getString("explain2"), rs
										.getInt("rank"), "", rs.getInt("mark"), "");
							}
						});

				if (resultsFromUWA.isEmpty()) {
					break;
				}

				// System.out.println("begin generateQuestion " + new Date());
				generateQuestion(userId, resultsFromUWA, MyConstants.FREE_QUIZ,
						startQuesIndex, wordIdSet);

				for (int i = 0; i < resultsFromUWA.size(); i++) {
					QuizWordBean qwbFromUWA = resultsFromUWA.get(i);
					if (!wordIdSet.contains(qwbFromUWA.getWordId())) {
						results.add(qwbFromUWA);
						if (results.size() >= pageSize) {
							break;
						}
					}
				}

			}
		}
		while (results.size() < pageSize);
		System.out.println("###finish getWordListForQuiz " + new Date());
		return results.size() > pageSize ? results.subList(0, pageSize) : results; // FIXME
	}

	public List<QuizRating> getRateListForQuiz(long quizId) {
		String sql = "SELECT  u.email, u.nickname, (sum(qr.is_right)/count(*)) * 100  rate FROM quiz_result qr, user u "
				+ " where qr.user_id = u.id and qr.quiz_id = ? "
				+ " group by qr.user_id  order by rate desc, qr.last_upt asc limit 0, 20";

		// System.out.println("begin getRateListForQuiz " + new Date());
		List<QuizRating> results = this.jdbcTemplate.query(sql, new Object[] { quizId },
				new RowMapper<QuizRating>() {
					@Override
					public QuizRating mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return new QuizRating(rs.getString("email"), rs
								.getString("nickname"), rs.getDouble("rate"));
					}
				});

		return results;
	}

	public double getRateForQuiz(long quizId, long userId) {
		String sql = "SELECT  (sum(qr.is_right)/count(*)) * 100  rate FROM quiz_result qr, user u "
				+ " where qr.user_id = u.id and qr.quiz_id = ? and u.id = ?  ";

		// System.out.println("begin getRateListForQuiz " + new Date());
		Double rate = this.jdbcTemplate.queryForObject(sql,
				new Object[] { quizId, userId }, Double.class);
		return rate == null ? 0 : rate.doubleValue();
	}

	private void generateQuestion(long userId, List<QuizWordBean> results, int mode,
			AtomicInteger startQuesIndex, Set<Long> wordIdSet) {
		for (int i = 0; i < results.size(); i++) {
			QuizWordBean qwb = results.get(i);
			Word word = this.wordRepository.findOne(qwb.getWordId());
			UserWordAsso uwa = this.userWordAssoRepository.findByUserIdAndWordId(userId,
					word.getId());
			boolean isEffective = true;

			if (uwa != null) {
				String effDateStr = uwa.getEffectDate();
				String currDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date());
				// System.out.println("effDateStr:" + effDateStr);
				// System.out.println("currDateStr:" + currDateStr);
				if (StringUtils.isEmpty(effDateStr)) {
					isEffective = true;
				}
				else {
					isEffective = currDateStr.compareTo(effDateStr) >= 0; // FIXME?
				}

				// if (uwa.getRank() <= 1) {
				// isEffective = false;
				// }
			}

			// System.out.println("isEffective:" + isEffective + " for word:"
			// + word.getValue());
			List<Sentence> senList = new ArrayList<Sentence>();
			if (mode == MyConstants.FREE_QUIZ) {
				senList = this.sentenceRepository.findForQuiz(userId, "% "
						+ word.getValue().toLowerCase() + " %");
				if (senList.isEmpty()) {
					senList = this.sentenceRepository.findInDummyArticle("% "
							+ word.getValue().toLowerCase() + " %");
				}

				if (senList.isEmpty()) {
					senList = this.sentenceRepository.findSubGrid4("% "
							+ word.getValue().toLowerCase() + " %", userId);
				}

			}
			int blankCount = 0;
			String answer = "";
			String wordExplain = word.getExplain2();
			if (word.getValue().length() <= 2
					|| (mode == MyConstants.FREE_QUIZ && senList.isEmpty())
					|| ("UNKNOWN".equals(wordExplain) || "TODO2".equals(wordExplain) || StringUtils
							.isEmpty(wordExplain))) {
				results.remove(i);
				i--;
			}
			else if (wordIdSet != null && wordIdSet.contains(word.getId())) {
				results.remove(i);
				i--;
			}
			else if (!isEffective) {
				results.remove(i);
				i--;
			}
			else {
				int startQuesIndexInt = startQuesIndex.incrementAndGet();
				String sentence = null;
				if (mode == MyConstants.FREE_QUIZ) {
					sentence = senList.get(0).getContent();
					qwb.setSenId(senList.get(0).getId());
				}
				else {
					sentence = qwb.getSentence();
				}
				String sentence2 = "";
				String[] words = sentence.split("\\s+");
				boolean isFound = false;
				for (int j = 0; j < words.length; j++) {
					if (!isFound && words[j].toLowerCase().equals(word.getLowValue())) {
						sentence2 += "&nbsp;";
						if (words[j].toLowerCase().endsWith("ing")) {
							answer = words[j].substring(0, words[j].length() - 3);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ startQuesIndexInt
									+ "' type='input' class='input-answer' onKeyUp='checkAnswer("
									+ startQuesIndexInt + ", " + blankCount
									+ ")' maxlength=" + blankCount + " /> " + "ing" + " ";
						}
						else if (words[j].toLowerCase().endsWith("es")) {
							answer = words[j].substring(0, words[j].length() - 2);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ startQuesIndexInt
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ startQuesIndexInt + ", " + blankCount
									+ ")' maxlength=" + blankCount + " /> " + "es" + " ";
						}
						else if (words[j].toLowerCase().endsWith("s")) {
							answer = words[j].substring(0, words[j].length() - 1);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ startQuesIndexInt
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ startQuesIndexInt + ", " + blankCount
									+ ")' maxlength=" + blankCount + "/> " + "s" + " ";
						}
						else if (words[j].toLowerCase().endsWith("ed")) {
							answer = words[j].substring(0, words[j].length() - 2);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ startQuesIndexInt
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ startQuesIndexInt + ", " + blankCount
									+ ")' maxlength=" + blankCount + " /> " + "ed" + " ";
						}
						else if (words[j].toLowerCase().endsWith("d")) {
							answer = words[j].substring(0, words[j].length() - 1);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ startQuesIndexInt
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ startQuesIndexInt + ", " + blankCount
									+ ")' maxlength=" + blankCount + " /> " + "d" + " ";
						}
						else { // if (qwb.getMark() == MyConstants.ORIGINAL_TENSE)
							answer = words[j].substring(1);
							blankCount = answer.length();
							sentence2 += words[j].substring(0, 1)
									+ "<input id='input"
									+ startQuesIndexInt
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ startQuesIndexInt + ", " + blankCount
									+ ")' maxlength=" + blankCount + " /> ";
						}
						isFound = true;
					}
					else {
						sentence2 += words[j] + " ";
					}
				}
				qwb.setSentence(sentence2);
				qwb.setBlankCount(blankCount);
				qwb.setAnswer(answer);

			}
		}
	}

	public void parseArticle(Long id, long userId, String stringText) {

		final Article article = this.articleRepository.findOne(id);
		String type = article.getType();

		String articleName = article.getName();
		// System.out.println("###name:" + articleName);
		// String filePath = "readme.txt";
		File uploadDir = new File("uploadFiles");
		File articlePath = new File(uploadDir + File.separator + userId, articleName);
		// System.out.println("articlePath:" + articlePath.getAbsolutePath());
		InputStream stream = null;
		try {
			String content = null;
			Tika tika = new Tika();
			if ("doc".equals(type)) {
				stream = new FileInputStream(articlePath);
				content = tika.parseToString(stream);

				// AutoDetectParser parser = new AutoDetectParser();
				// BodyContentHandler handler = new BodyContentHandler(Integer.MAX_VALUE);
				// Metadata metadata = new Metadata();
				// try {
				// parser.parse(stream, handler, metadata);
				// content = handler.toString();
				// }
				// finally {
				// stream.close();
				// }
			}
			else if ("webpage".equals(type)) {
				HtmlUnitDriver driver = new HtmlUnitDriver();
				driver.get(article.getUrl());
				String pageSource = driver.getPageSource();
				// try {
				// pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
				// }
				// catch (UnsupportedEncodingException e) {
				// throw new UnsupportedOperationException("Auto-generated method stub",
				// e);
				// }
				System.out.println("###pageSource1:" + pageSource);
				driver.close();
				File tempFile = new File("uploadFiles/tmp/" + System.currentTimeMillis()
						+ ".txt");
				FileUtils.writeStringToFile(tempFile, pageSource, "UTF-8");
				stream = new FileInputStream(tempFile);
				content = tika.parseToString(stream);
			}
			else if ("string".equals(type)) {
				content = stringText;
			}
			content = content.replaceAll("-(\r)?\n", "");
			content = content.replaceAll("(\r)?\n", " ");
			content = content.replace("-LRB-", "(");
			content = content.replace("-RRB-", ")");
			content = content.replace("Â¡Â¯", "'");
			System.out.println("###content:" + content);

			File tempFile = new File(uploadDir, System.currentTimeMillis() + ".txt");
			FileUtils.writeStringToFile(tempFile, content, "UTF-8");
			// option #1: By sentence.
			DocumentPreprocessor dp = new DocumentPreprocessor(tempFile.getAbsolutePath());
			final Set<Sentence> sentencesSet = new HashSet<Sentence>();
			final List<ArticleWordAsso> awaList = new ArrayList<ArticleWordAsso>();
			// System.out.println("begin parse sentence");
			// ExecutorService exec = Executors.newFixedThreadPool(1); // FIXME
			for (List sentence : dp) {
				final List sentenceFinal = sentence;
				// exec.execute(new Runnable() {
				// @Override
				// public void run() {
				parseSentence(article, userId, sentencesSet, sentenceFinal, awaList);
				// }
				// });
			}

			// exec.shutdown();
			// exec.awaitTermination(1, TimeUnit.DAYS);
			// System.out.println("begin save  ArticleWordAsso");
			for (ArticleWordAsso awa : awaList) {
				this.articleWordAssoRepository.save(awa);
			}

			// System.out.println("###parse done");
			article.setSentences(sentencesSet);
			this.articleRepository.save(article);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			IOUtils.closeQuietly(stream);
		}

	}

	private void parseSentence(Article article, long userId, Set<Sentence> sentencesSet,
			List sentence, List<ArticleWordAsso> awaList) {
		// System.out.println("###sentence:" + sentence);
		String sentenceStr = "";
		for (int i = 0; i < sentence.size(); i++) {
			Object oneWord = sentence.get(i);
			sentenceStr += (oneWord + " ");

			String lowCaseWord = oneWord.toString().toLowerCase();
			// TODO MARK WORD -1 = ignore
			if ("...".equals(lowCaseWord) || ".".equals(lowCaseWord)
					|| "-RRB-".equals(lowCaseWord) || "-".equals(lowCaseWord)
					|| ",".equals(lowCaseWord) || "■".equals(lowCaseWord)
					|| !lowCaseWord.matches("^[a-zA-Z\\-]*")) {
				// System.out.println("ignore:" + lowCaseWord);
			}
			else {
				List<Word> dbWordList = this.wordRepository.findByLowValue(oneWord
						.toString().toLowerCase());
				Word dbWord = dbWordList.isEmpty() ? null : dbWordList.get(0);
				if (dbWord == null) {
					dbWord = new Word();
					dbWord.setValue(oneWord.toString());
					dbWord.setLowValue(oneWord.toString().toLowerCase());
					dbWord.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()));

					fillWordInfo(dbWord);
					this.wordRepository.save(dbWord);
				}
				else {
					if (StringUtils.isEmpty(dbWord.getExplain2())) {
						fillWordInfo(dbWord);
						this.wordRepository.save(dbWord);
					}
				}

				ArticleWordAsso awa = null;
				List<ArticleWordAsso> awaDBList = this.articleWordAssoRepository
						.findByArticleIdAndWordId(article.getId(), dbWord.getId());

				UserWordAsso uwa = this.userWordAssoRepository.findByUserIdAndWordId(
						userId, dbWord.getId());

				if (awaDBList.isEmpty()) {
					awa = new ArticleWordAsso();
					awa.setArticle(article);
					awa.setWord(dbWord);
					awa.setHit(1);
				}
				else {
					awa = awaDBList.get(0);
					awa.setHit(awa.getHit() + 1);
				}
				awa.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));

				if (uwa == null) {
					uwa = new UserWordAsso();
					User user = new User();
					user.setId(userId);
					uwa.setUser(user);
					uwa.setWord(dbWord);
					uwa.setRank(5 - (dbWord.getLevel() == null ? 0 : dbWord.getLevel()));
					uwa.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()));
					this.userWordAssoRepository.save(uwa);
				}

				// this.articleWordAssoRepository.save(awa);
				awaList.add(awa);
			}

			boolean isLastWord = (i == sentence.size() - 1);
			String nextWord = "";
			if (i + 1 < sentence.size()) {
				nextWord = sentence.get(i + 1).toString();
			}
			if (sentenceStr.length() > 250
					|| (sentenceStr + " " + nextWord).length() > 250 || isLastWord) {
				Sentence oneSentence = new Sentence();
				sentenceStr = sentenceStr.replace("-LRB-", "(");
				sentenceStr = sentenceStr.replace("-RRB-", ")");
				sentenceStr = sentenceStr.replace("Â¡Â¯", "'");
				if (StringUtils.isNotBlank(sentenceStr)) {
					oneSentence.setContent(" " + sentenceStr + " ");
					oneSentence.setArticle(article);
					oneSentence.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()));
					this.sentenceRepository.save(oneSentence);
					sentencesSet.add(oneSentence);

					sentenceStr = "";
				}
			}

		}
	}

	public void test() {
		Article article = this.articleRepository.findOne(6L);
		for (Sentence oldSen : article.getSentences()) {
			// System.out.println("###remove oldSen:" + oldSen.getId());
			this.sentenceRepository.delete(oldSen.getId());
		}
	}

	/**
	 * @param l
	 */
	public void removeOldData(long id) {
		Article article = this.articleRepository.findOne(id);
		Set<Sentence> sentences = article.getSentences();
		if (sentences != null) {
			for (Sentence oldSen : sentences) {
				// System.out.println("###remove oldSen:" + oldSen.getId());
				this.userSenAssoRepository.deleteBySenId(oldSen.getId());
				this.sentenceRepository.delete(oldSen.getId());
			}
		}

		List<ArticleWordAsso> words = article.getWords();
		if (words != null) {
			for (ArticleWordAsso oldAwa : words) {
				// System.out.println("###remove oldAwa:" + oldAwa);
				this.articleWordAssoRepository.delete(oldAwa);
			}
		}

	}

	// public List<ArticleBean> getArticleList0() {
	// List<ArticleBean> articleList = new ArrayList<ArticleBean>();
	// Iterable<Article> articleIt = this.articleRepository.findAll();
	//
	// for (Article article : articleIt) {
	// ArticleBean articleBean = new ArticleBean();
	// BeanUtils.copyProperties(article, articleBean);
	// if (article.getUser() != null) {
	// articleBean.setUserName(article.getUser().getName());
	// }
	// articleList.add(articleBean);
	// }
	// return articleList;
	// }

	public JqGridData<Article> getArticleList(long userId, int rows, int currPage,
			String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
			sord = "desc";
		}
		List<Article> articleList = this.articleRepository.findByUserId(
				userId,
				new PageRequest(currPage - 1, rows, "asc".equals(sord) ? Direction.ASC
						: Direction.DESC, sidx)).getContent();
		long totalCount = this.articleRepository.getUserTotalCount(userId);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<Article> gridData = new JqGridData<Article>(totalNumberOfPages,
				currPage, totalNumberOfRecords, articleList);
		return gridData;
	}

	public JqGridData<ArticleBean> getArticleList2(long userId, int rows, int currPage,
			String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
			sord = "desc";
		}
		String sql = "SELECT a.id, a.url, a.name, a.remark, a.open_flag, a.type, a.delete_flag , a.last_upt, uafa.status "
				+ " FROM article a, user_article_fork_asso uafa where a.id = uafa.article_id "
				+ " and uafa.user_id = ? and a.delete_flag != 1 and a.id != -1 order by uafa.status asc,  a.last_upt desc limit "
				+ (currPage - 1) * 10 + ", " + rows;
		String countSql = "SELECT count(*) "
				+ " FROM article a, user_article_fork_asso uafa where a.id = uafa.article_id "
				+ " and uafa.user_id = ? and a.delete_flag != 1 and a.id != -1 ";

		List<ArticleBean> articleList = this.jdbcTemplate.query(sql,
				new Object[] { userId }, new RowMapper<ArticleBean>() {
					@Override
					public ArticleBean mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return new ArticleBean(rs.getLong("id"), rs.getString("url"), rs
								.getString("name"), rs.getString("remark"), rs
								.getString("open_flag"), rs.getString("type"), rs
								.getInt("delete_flag"), rs.getString("last_upt"), rs
								.getString("status"));
					}
				});
		long totalCount = this.jdbcTemplate.queryForObject(countSql,
				new Object[] { userId }, Long.class);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<ArticleBean> gridData = new JqGridData<ArticleBean>(
				totalNumberOfPages, currPage, totalNumberOfRecords, articleList);
		return gridData;
	}

	public JqGridData<ArticleBean> getArticleList2Others(long userId, int rows,
			int currPage, String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
			sord = "desc";
		}
		// String sql =
		// "SELECT a.id, a.url, a.name, a.remark, a.open_flag, a.type, a.delete_flag , a.last_upt, uafa.status "
		// + " FROM article a, user_article_fork_asso uafa where a.id = uafa.article_id "
		// +
		// " and uafa.user_id != ? and a.delete_flag != 1 and a.id != -1 order by uafa.status asc,  a.last_upt desc limit "
		// + (currPage - 1) * 10 + ", " + rows;
		// String countSql = "SELECT count(*) "
		// + " FROM article a, user_article_fork_asso uafa where a.id = uafa.article_id "
		// + " and uafa.user_id != ? and a.delete_flag != 1 and a.id != -1 ";

		String sql = "SELECT a.id, a.url, a.name, a.remark, a.open_flag, a.type, a.delete_flag , a.last_upt, '' status "
				+ " FROM article a where  "
				+ " a.user_id != ? and a.delete_flag != 1 and a.id != -1 order by  a.last_upt desc limit "
				+ (currPage - 1) * 10 + ", " + rows;
		String countSql = "SELECT count(*) " + " FROM article a where  "
				+ " a.user_id != ? and a.delete_flag != 1 and a.id != -1";

		List<ArticleBean> articleList = this.jdbcTemplate.query(sql,
				new Object[] { userId }, new RowMapper<ArticleBean>() {
					@Override
					public ArticleBean mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return new ArticleBean(rs.getLong("id"), rs.getString("url"), rs
								.getString("name"), rs.getString("remark"), rs
								.getString("open_flag"), rs.getString("type"), rs
								.getInt("delete_flag"), rs.getString("last_upt"), rs
								.getString("status"));
					}
				});
		long totalCount = this.jdbcTemplate.queryForObject(countSql,
				new Object[] { userId }, Long.class);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<ArticleBean> gridData = new JqGridData<ArticleBean>(
				totalNumberOfPages, currPage, totalNumberOfRecords, articleList);
		return gridData;
	}

	public ArticleBean getArticle(long userId, long articleId) {
		// String sql =
		// "SELECT a.id, a.url, a.name, a.remark, a.open_flag, a.type, a.delete_flag , a.last_upt, uafa.status "
		// +
		// " FROM article a left join user_article_fork_asso uafa where a.id = uafa.article_id "
		// + " and uafa.user_id = ? and a.id= ? and a.delete_flag != 1 ";
		String sql = "SELECT a.id, a.url, a.name, a.remark, a.open_flag, a.type, a.delete_flag , a.last_upt, uafa.status, "
				+ " uafa.user_id FROM article a left outer join user_article_fork_asso uafa on a.id = uafa.article_id and uafa.user_id = ? "
				+ " where a.id= ? and a.delete_flag != 1";

		List<ArticleBean> articleList = this.jdbcTemplate.query(sql, new Object[] {
				userId, articleId }, new RowMapper<ArticleBean>() {
			@Override
			public ArticleBean mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new ArticleBean(rs.getLong("id"), rs.getString("url"), rs
						.getString("name"), rs.getString("remark"), rs
						.getString("open_flag"), rs.getString("type"), rs
						.getInt("delete_flag"), rs.getString("last_upt"), rs
						.getString("status"));
			}
		});
		return articleList.isEmpty() ? null : articleList.get(0);
	}

	public Audio getAudio(long audioId) {
		Audio audio = this.audioRepository.findOne(audioId);
		return audio;
	}

	public JqGridData<Quiz> getQuizList(int rows, int currPage, String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "hot";
			sord = "desc";
		}
		rows = 100; // FIXME
		Pageable pageable = new PageRequest(currPage - 1, rows,
				"asc".equals(sord) ? Direction.ASC : Direction.DESC, sidx);
		List<Quiz> quizList = this.quizRepository.findAll(pageable).getContent();
		List<Quiz> quizList2 = new ArrayList<Quiz>();

		int quizSize = quizList.size();
		for (int i = 0; i < quizSize; i++) {
			Quiz quiz = quizList.get(i);
			if (quiz.getDeleteFlag() != 1) {
				// if (StringUtils.isNotEmpty(quiz.getPassword())) {
				// quiz.setPassword("*******");
				// }
				quizList2.add(quiz);
			}
		}

		// long totalCount = this.quizRepository.count();
		long totalCount = quizList2.size();
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<Quiz> gridData = new JqGridData<Quiz>(totalNumberOfPages, currPage,
				totalNumberOfRecords, quizList2);
		return gridData;
	}

	public JqGridData<Audio> getAudioList(int rows, int currPage, String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
			sord = "desc";
		}
		Pageable pageable = new PageRequest(currPage - 1, rows,
				"asc".equals(sord) ? Direction.ASC : Direction.DESC, sidx);
		List<Audio> quizList = this.audioRepository.findAll(pageable).getContent();

		long totalCount = quizList.size();
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<Audio> gridData = new JqGridData<Audio>(totalNumberOfPages, currPage,
				totalNumberOfRecords, quizList);
		return gridData;
	}

	public JqGridData<AudioSnippet> getAudioSnippetList(int rows, int currPage,
			String sidx, String sord, long audioId) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "start";
			sord = "asc";
		}
		Pageable pageable = new PageRequest(currPage - 1, rows,
				"asc".equals(sord) ? Direction.ASC : Direction.DESC, sidx);
		List<AudioSnippet> quizList = this.audioSnippetRepository
				.findByAudioIdOrderByStartAsc(pageable, audioId).getContent();

		long totalCount = quizList.size();
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<AudioSnippet> gridData = new JqGridData<AudioSnippet>(
				totalNumberOfPages, currPage, totalNumberOfRecords, quizList);
		return gridData;
	}

	public Quiz getQuiz(long qId, String password) {
		Quiz quiz = this.quizRepository.findOne(qId);
		if (StringUtils.isEmpty(quiz.getPassword())) {
			return quiz;
		}
		else if (StringUtils.equals(quiz.getPassword(),
				this.passwordEncoder.encodePassword(password, null))) {
			return quiz;
		}
		else {
			Quiz fakeQuiz = new Quiz();
			fakeQuiz.setId(-1L);
			return fakeQuiz;
		}
	}

	/**
	 * @param userId
	 * @param rows
	 * @param page
	 * @return
	 */
	public JqGridData<SenSummary> getSenList(long userId, int rows, int currPage,
			String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
		}
		List<SenSummary> senList = this.sentenceRepository.findByArticleUserId(
				userId,
				new PageRequest(currPage - 1, rows, "asc".equals(sord) ? Direction.ASC
						: Direction.DESC, sidx)).getContent();
		long totalCount = this.sentenceRepository.getUserTotalCount(userId);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<SenSummary> gridData = new JqGridData<SenSummary>(totalNumberOfPages,
				currPage, totalNumberOfRecords, senList);
		return gridData;
	}

	/**
	 * @param userId
	 * @param rows
	 * @param page
	 * @return
	 */
	public JqGridData<SenBean> getSenList2(long articleId, long userId, int rows,
			int currPage) {
		// List<SenSummary> senList = this.sentenceRepository.findByArticleUserId2(
		// articleId,
		// userId,
		// new PageRequest(currPage - 1, rows, "asc".equals(sord) ? Direction.ASC
		// : Direction.DESC, sidx)).getContent();

		UserArticleForkAsso uafa = this.userArticleForkAssoRepository
				.findByUserIdAndArticleId(userId, articleId);
		if (uafa == null) {
			uafa = new UserArticleForkAsso();
			Article article = new Article();
			article.setId(articleId);
			uafa.setArticle(article);
			User user = new User();
			user.setId(userId);
			uafa.setUser(user);
			uafa.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date()));
			this.userArticleForkAssoRepository.save(uafa);
		}

		// String sql = "select sen.id, sen.content, usa.useful_flag "
		// + " from sentence sen left outer join user_sen_asso usa on sen.id=usa.sen_id "
		// + "   join article a where "
		// + " sen.article_id=a.id "
		// // + " and usa.user_id= ?"
		// + " and a.delete_flag<>1 " + " and a.hide_flag='No' "
		// + " and sen.article_id= ? order by sen.id asc limit " + (currPage - 1)
		// * 10 + ", " + rows;

		String sql = "select * from ( " + " select sen.id senId, sen.content senContent "
				+ " from sentence sen  " + "  join article a where"
				+ "  sen.article_id=a.id and sen.article_id = ?"
				+ " and a.delete_flag<>1 "
				+ " and a.hide_flag='No') sen2 left join  user_sen_asso usa"
				+ " on sen2.senId = usa.sen_id and usa.user_id = ?"
				+ "  order by sen2.senId asc limit " + (currPage - 1) * 10 + ", " + rows;

		// String countSql = "select count(*) "
		// + " from sentence sen left outer join user_sen_asso usa on sen.id=usa.sen_id "
		// + "   join article a where " + " sen.article_id=a.id"
		// // + " and usa.user_id= ? "
		// + " and a.delete_flag<>1 " + " and a.hide_flag='No' "
		// + " and sen.article_id= ?  ";

		String countSql = "select count(*) from ( "
				+ " select sen.id senId, sen.content senContent "
				+ " from sentence sen  " + "  join article a where"
				+ "  sen.article_id=a.id and sen.article_id = ?"
				+ " and a.delete_flag<>1 "
				+ " and a.hide_flag='No') sen2 left join  user_sen_asso usa"
				+ " on sen2.senId = usa.sen_id and usa.user_id = ?";

		System.out.println("countSql:" + countSql);
		List<SenBean> senList = this.jdbcTemplate.query(sql, new Object[] { articleId,
				userId }, new RowMapper<SenBean>() {
			@Override
			public SenBean mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new SenBean(rs.getLong("senId"), rs.getString("senContent"), rs
						.getString("useful_flag"));
			}
		});

		long totalCount = this.jdbcTemplate.queryForObject(countSql, new Object[] {
				articleId, userId }, Long.class);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<SenBean> gridData = new JqGridData<SenBean>(totalNumberOfPages,
				currPage, totalNumberOfRecords, senList);
		return gridData;
	}

	public JqGridData<Sentence> getSenListForSubGrid(long wordId, long userId) {
		Word word = this.wordRepository.findOne(wordId);
		List<Sentence> senList = this.sentenceRepository.findByArticleUserIdSubGrid(
				userId, "% " + word.getValue().toLowerCase() + " %");
		long totalCount = 0;
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = 0;

		long currPage = 0;
		JqGridData<Sentence> gridData = new JqGridData<Sentence>(totalNumberOfPages,
				currPage, totalNumberOfRecords, senList);
		return gridData;
	}

	public JqGridData<Sentence> getSenListForSubGrid2(long wordId, long articleId,
			long userId) {
		Word word = this.wordRepository.findOne(wordId);
		List<Sentence> senList = this.sentenceRepository.findByArticleUserIdSubGrid2(
				userId, articleId, "% " + word.getValue().toLowerCase() + " %");
		long totalCount = 0;
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = 0;

		long currPage = 0;
		JqGridData<Sentence> gridData = new JqGridData<Sentence>(totalNumberOfPages,
				currPage, totalNumberOfRecords, senList);
		return gridData;
	}

	/**
	 * look for sen list that belong to this user
	 * @param wordId
	 * @param userId
	 * @return
	 */
	public JqGridData<Sentence> getSenListForSubGrid3(long wordId, long userId) {
		Word word = this.wordRepository.findOne(wordId);
		List<Sentence> senList = this.sentenceRepository.findSubGrid3(userId, "% "
				+ word.getValue().toLowerCase() + " %");

		if (senList.isEmpty()) {
			return getSenListForSubGrid4(wordId, userId);
		}

		long totalCount = 0;
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = 0;

		long currPage = 0;
		JqGridData<Sentence> gridData = new JqGridData<Sentence>(totalNumberOfPages,
				currPage, totalNumberOfRecords, senList);
		return gridData;
	}

	/**
	 * look for sen list that don't belong to this user
	 * @param wordId
	 * @param userId
	 * @return
	 */
	public JqGridData<Sentence> getSenListForSubGrid4(long wordId, long userId) {
		Word word = this.wordRepository.findOne(wordId);
		List<Sentence> senList = this.sentenceRepository.findSubGrid4("% "
				+ word.getValue().toLowerCase() + " %", userId);
		long totalCount = 0;
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = 0;

		long currPage = 0;
		JqGridData<Sentence> gridData = new JqGridData<Sentence>(totalNumberOfPages,
				currPage, totalNumberOfRecords, senList);
		return gridData;
	}

	/**
	 * @param userId
	 * @param rows
	 * @param page
	 * @return
	 */
	public JqGridData<Word> getWordList(long userId, int rows, int currPage, String sidx,
			String sord) {
		// List<WordSummary> wordList = null;
		List<Word> wordList = null;
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
		}
		// if ("hit".equals(sidx)) {
		// wordList = this.wordRepository.findByUserId(userId,
		// new PageRequest(currPage - 1, rows)).getContent(); // ,
		String sort = sidx + " " + sord;
		wordList = this.wordRepository.findByUserId3(userId, (currPage - 1) * rows, rows,
				sort);

		// for (int i = 0; i < wordList.size(); i++) {
		// Word word = wordList.get(i);
		// // word.setTempRank(parseRank(word.getTempRank()));
		// }

		// ,
		// "asc".equals(sord)
		// }
		// else {
		// wordList = this.wordRepository.findByUserId(
		// userId,
		// new PageRequest(currPage - 1, rows,
		// "asc".equals(sord) ? Direction.ASC : Direction.DESC, sidx))
		// .getContent(); // ,
		// }

		long totalCount = this.wordRepository.getWordTotalCount(userId);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<Word> gridData = new JqGridData<Word>(totalNumberOfPages, currPage,
				totalNumberOfRecords, wordList);
		return gridData;
	}

	public JqGridData<Word> getWordList2(long userId, long articleId, int rows,
			int currPage, String sidx, String sord) {
		// List<WordSummary> wordList = null;
		List<Word> wordList = null;
		wordList = this.wordRepository.findByUserId4(userId, articleId, (currPage - 1)
				* rows, rows);

		long totalCount = this.wordRepository.getWordTotalCount2(userId, articleId);
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<Word> gridData = new JqGridData<Word>(totalNumberOfPages, currPage,
				totalNumberOfRecords, wordList);
		return gridData;
	}

	public JqGridData<Word> getWordList2ForSingle(long userId, long articleId,
			String wordValue) {
		// List<WordSummary> wordList = null;
		List<Word> wordList = null;
		wordList = this.wordRepository.findByUserId5(userId, wordValue);
		// FIXME 频次统计不对，目前暂时不显示

		long totalCount = wordList.size();
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = totalCount;

		JqGridData<Word> gridData = new JqGridData<Word>(totalNumberOfPages, 1,
				totalNumberOfRecords, wordList);
		return gridData;
	}

	public JqGridData<WordBean2> getMyWordList(long userId, int rows, int currPage,
			String sidx, String sord) {
		String currDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		System.out.println("currDateStr:" + currDateStr);
		// List<WordSummary> wordList = null;
		String sql = "SELECT distinct w.id, w.explain2, w.value, w.pron, w.level, w.audio_path, uwa2.last_upt2, uwa2.effect_date from word w , ( "
				+ " select word_id, max(last_upt) last_upt2 , effect_date from user_word_asso  where interest = 1 and (effect_date is null or effect_date <= ? or effect_date > '3000') and user_id = ?  group by word_id "
				+ " ) uwa2 where w.id = uwa2.word_id  order by uwa2.last_upt2 desc limit "
				+ (currPage - 1) * 10 + ", " + rows;

		String countSql = "select count(*) from (SELECT distinct w.id, w.explain2, w.value, w.pron, w.level, uwa2.last_upt2 from word w , ( "
				+ " select word_id, max(last_upt) last_upt2 from user_word_asso where interest = 1 and (effect_date is null or effect_date <= ?  or effect_date > '3000')  and user_id = ?  group by word_id "
				+ " ) uwa2 where w.id = uwa2.word_id) tt ";

		System.out.println("sql:" + sql);
		System.out.println("countSql:" + countSql);
		// System.out.println("begin getWordListFromQuizResult " + new Date());
		List<WordBean2> results = this.jdbcTemplate.query(sql, new Object[] {
				currDateStr, userId }, new RowMapper<WordBean2>() {
			@Override
			public WordBean2 mapRow(ResultSet rs, int rowNum) throws SQLException {
				String audioPath = rs.getString("audio_path");
				String content = rs.getString("value");
				long wordId = rs.getLong("id");
				// String[] contentCharArr = content.split("");
				// String contentCharStr = "";
				// for (int i = 0; i < contentCharArr.length; i++) {
				// contentCharStr += contentCharArr[i] + ", ";
				// }
				// String content2 = content + " " + contentCharStr + " " + content;
				// if (StringUtils.isBlank(audioPath)) {
				// audioPath = MyUtil.genAudioFile(
				// ArticleServiceImpl.this.mySettings.getAudioDir(), content2);
				// }
				//
				// if (StringUtils.isNotBlank(audioPath)) {
				// Word word = ArticleServiceImpl.this.wordRepository.findOne(wordId);
				// word.setAudioPath(audioPath);
				// word.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				// .format(new Date()));
				// ArticleServiceImpl.this.wordRepository.save(word);
				// }
				return new WordBean2(wordId, content, rs.getString("explain2"), rs
						.getString("pron"), audioPath, rs.getString("last_upt2"));

			}
		});

		long totalCount = this.jdbcTemplate.queryForObject(countSql, new Object[] {
				currDateStr, userId }, Long.class);

		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<WordBean2> gridData = new JqGridData<WordBean2>(totalNumberOfPages,
				currPage, totalNumberOfRecords, results);
		return gridData;
	}

	public JqGridData<WordBean2> getOthersWordList(long userId, int rows, int currPage,
			String sidx, String sord) {
		String currDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		System.out.println("currDateStr:" + currDateStr);
		// List<WordSummary> wordList = null;
		String sql = "SELECT distinct w.id, w.explain2, w.value, w.pron, w.level, w.audio_path, uwa2.last_upt2, uwa2.effect_date from word w , ( "
				+ " select word_id, max(last_upt) last_upt2 , effect_date from user_word_asso  where interest = 1 and user_id != ? "
				+ " and word_id not in( select word_id from user_word_asso where (interest = 1 or interest = -1) and user_id = ? )  group by word_id "
				+ " ) uwa2 where w.id = uwa2.word_id  order by uwa2.last_upt2 desc limit "
				+ (currPage - 1) * 10 + ", " + rows;

		String countSql = "select count(*) from (SELECT distinct w.id, w.explain2, w.value, w.pron, w.level, uwa2.last_upt2 from word w , ( "
				+ " select word_id, max(last_upt) last_upt2 from user_word_asso where interest = 1 and user_id != ? "
				+ "  and word_id not in( select word_id from user_word_asso where  (interest = 1 or interest = -1) and user_id = ? )  group by word_id "
				+ " ) uwa2 where w.id = uwa2.word_id) tt ";

		System.out.println("sql:" + sql);
		System.out.println("countSql:" + countSql);
		// System.out.println("begin getWordListFromQuizResult " + new Date());
		List<WordBean2> results = this.jdbcTemplate.query(sql, new Object[] { userId,
				userId }, new RowMapper<WordBean2>() {
			@Override
			public WordBean2 mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new WordBean2(rs.getLong("id"), rs.getString("value"), rs
						.getString("explain2"), rs.getString("pron"), rs
						.getString("audio_path"), rs.getString("last_upt2"));

			}
		});

		long totalCount = this.jdbcTemplate.queryForObject(countSql, new Object[] {
				userId, userId }, Long.class);

		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<WordBean2> gridData = new JqGridData<WordBean2>(totalNumberOfPages,
				currPage, totalNumberOfRecords, results);
		return gridData;
	}

	public String getMyReminderInfo(long userId) {
		User user = this.userRepository.findOne(userId);
		String wxRemiderHourStr = user.getWxRemiderHour();
		return StringUtils.isBlank(wxRemiderHourStr) ? "" : wxRemiderHourStr;
	}

	public String saveMyReminderInfo(long userId, String hour) {
		User user = this.userRepository.findOne(userId);
		user.setWxRemiderHour(hour);
		this.userRepository.save(user);
		return "保存成功";
	}

	public Integer[] getMyWordSummary(long userId) {
		String currDateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		System.out.println("currDateStr:" + currDateStr);
		// List<WordSummary> wordList = null;
		String sql = "select a.user_id, a.word_id, previousCount from ( "
				+ " SELECT user_id, word_id FROM quiz_result where user_id = ? "
				+ " and last_upt > ? and is_right = 1 " + " group by user_id, word_id "
				+ " ) a left join ( "
				+ " SELECT user_id, word_id, count(*) previousCount FROM quiz_result "
				+ " where user_id = ?  and last_upt < ? group by user_id, word_id"
				+ " ) b on a.word_id = b.word_id";

		System.out.println("sql:" + sql);
		// System.out.println("begin getWordListFromQuizResult " + new Date());
		List<MyWordSummary> results = this.jdbcTemplate.query(sql, new Object[] { userId,
				currDateStr, userId, currDateStr }, new RowMapper<MyWordSummary>() {
			@Override
			public MyWordSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new MyWordSummary(rs.getLong("user_id"), rs.getLong("word_id"), rs
						.getInt("previousCount"));

			}
		});

		int newWordCount = 0;
		int todayLearnSize = results.size();
		for (int i = 0; i < todayLearnSize; i++) {
			if (results.get(i).getPreviousCount() == 0) {
				newWordCount++;
			}
		}

		return new Integer[] { newWordCount, todayLearnSize };
	}

	public JqGridData<SenBean> getMySenList(long userId, int rows, int currPage,
			String sidx, String sord) {
		String audioDirStr = this.mySettings.getAudioSnippetDir();
		System.out.println("mySettings.getAudioDir():" + audioDirStr);

		String sql = "SELECT s.id, s.content, s.last_upt, s.audio_path FROM sentence s, user_sen_asso usa "
				+ " where s.id = usa.sen_id "
				+ " and usa.user_id = ? and usa.useful_flag = 'Yes' "
				+ " order by usa.last_upt desc  "
				+ " limit "
				+ (currPage - 1)
				* 10
				+ ", " + rows;

		String countSql = "SELECT count(*) FROM sentence s, user_sen_asso usa "
				+ " where s.id = usa.sen_id "
				+ " and usa.user_id = ? and usa.useful_flag = 'Yes' ";

		System.out.println("sql:" + sql);
		System.out.println("countSql:" + countSql);
		// System.out.println("begin getWordListFromQuizResult " + new Date());
		List<SenBean> results = this.jdbcTemplate.query(sql, new Object[] { userId },
				new RowMapper<SenBean>() {
					@Override
					public SenBean mapRow(ResultSet rs, int rowNum) throws SQLException {
						String audioPath = rs.getString("audio_path");
						String content = rs.getString("content");
						long senId = rs.getLong("id");
						// if (StringUtils.isBlank(audioPath)) {
						// audioPath = MyUtil.genAudioFile(
						// ArticleServiceImpl.this.mySettings.getAudioDir(),
						// content);
						// }
						// if (StringUtils.isNotBlank(audioPath)) {
						// Sentence senDB = ArticleServiceImpl.this.sentenceRepository
						// .findOne(senId);
						// senDB.setAudioPath(audioPath);
						// senDB.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						// .format(new Date()));
						// ArticleServiceImpl.this.sentenceRepository.save(senDB);
						// }
						return new SenBean(senId, content, "", audioPath, rs
								.getString("last_upt"));

					}
				});

		long totalCount = this.jdbcTemplate.queryForObject(countSql,
				new Object[] { userId }, Long.class);

		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<SenBean> gridData = new JqGridData<SenBean>(totalNumberOfPages,
				currPage, totalNumberOfRecords, results);
		return gridData;
	}

	public synchronized void enhanceWord() {
		int index = 1;
		Iterable<Word> wordIt = this.wordRepository.findAll();
		for (Word word : wordIt) {
			if (StringUtils.isNotEmpty(word.getExplain2())) {
				continue;
			}
			// System.out.println(index++ + ":" + word);

			fillWordInfo(word);
			this.wordRepository.save(word);
		}

		// System.out.println("##enhanceWord done");

	}

	/**
	 * @param word
	 */
	public void fillWordInfo(Word word) {
		// System.out.println("fillWordInfo for word:" + word.getValue());
		WebDriver driver = new HtmlUnitDriver();
		String wordVal = word.getValue();
		driver.get("http://dict.cn/" + wordVal);
		String pageSource = driver.getPageSource();
		String pronStr = "";
		// System.out.println(pageSource);
		String explain = "";
		if (pageSource.contains("您要查找的是不是")) {
			explain = "UNKNOWN";
		}
		else {
			WebElement div = null;
			try {
				div = driver.findElement(By.xpath("//div[@class='basic clearfix']"));
				explain = div.getText();
			}
			catch (Exception e) {
				explain = "TODO2";
			}

			if (!"TODO2".equals(explain)) {
				try {
					List<WebElement> pronList = driver.findElements(By
							.xpath("//bdo[@lang='EN-US']"));
					for (int i = 0; i < pronList.size(); i++) {
						pronStr += pronList.get(i).getText() + " ";
					}
				}
				catch (Exception e) {
					pronStr = "UNKNOWN";
				}
			}
		}

		pronStr = StringUtils.trim(pronStr);
		// System.out.println("word:" + wordVal + "	expain:" + explain);
		word.setExplain(StringUtils.substring(explain, 250));
		word.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		word.setPron(pronStr);
	}

	/**
	 * @param article
	 * @param oper
	 * @return
	 */
	@Transactional
	public boolean saveOrUpdate(String ids, Article article, String oper, long userId,
			String stringText) {
		if ("edit".equals(oper)) {
			Article articleDB = this.articleRepository.findOne(Long.parseLong(ids));
			articleDB.setName(article.getName());
			articleDB.setType(article.getType());
			articleDB.setUrl(article.getUrl());
			articleDB.setOpenFlag(article.getOpenFlag());
			articleDB.setHideFlag(article.getHideFlag());
			articleDB.setRemark(article.getRemark());
			String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			articleDB.setLastUpt(lastUpt);
			this.articleRepository.save(articleDB);
		}
		if ("del".equals(oper)) {
			String[] idArr = StringUtils.split(ids, ",");
			for (int i = 0; i < idArr.length; i++) {
				long articleId = Long.parseLong(idArr[i]);
				Article articleInDB = this.articleRepository.findOne(articleId);
				User userInDB = articleInDB.getUser();
				if (userInDB != null && userInDB.getId() == userId) {
					// removeOldData(articleId);
					// this.articleRepository.delete(articleId);
					Article articleDB = this.articleRepository.findOne(articleId);
					articleDB.setDeleteFlag(1);
					this.articleRepository.save(article);
				}
				else {
					// System.out.println("you don't priv to remove this article: "
					// + articleId);
					return false;
				}

			}
		}
		else if ("add".equals(oper)) {
			User user = this.userRepository.findOne(userId);
			article.setUser(user);
			String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			article.setLastUpt(lastUpt);
			article.setDeleteFlag(0);
			article.setHideFlag("No");

			this.articleRepository.save(article);
			final long articleId = article.getId();

			UserArticleForkAsso uafa = new UserArticleForkAsso();
			uafa.setArticle(article);
			uafa.setUser(user);
			uafa.setLastUpt(lastUpt);
			this.userArticleForkAssoRepository.save(uafa);

			// FIXME async
			removeOldData(articleId);
			parseArticle(articleId, userId, stringText);

		}

		return true;
	}

	public String toggleMarkSen(long senId, long userId) {
		// Sentence sen = this.sentenceRepository.findOne(senId);
		UserSenAsso usa = this.userSenAssoRepository.findByUserIdAndSenId(userId, senId);
		if (usa == null) {
			usa = new UserSenAsso();
			usa.setUser(this.userRepository.findOne(userId));
			usa.setSen(this.sentenceRepository.findOne(senId));
			this.userSenAssoRepository.save(usa);
		}
		String usefulFlag = usa.getUsefulFlag();
		if (StringUtils.isEmpty(usefulFlag) || "No".equals(usefulFlag)) {
			usa.setUsefulFlag("Yes");
		}
		else {
			usa.setUsefulFlag("No");
		}
		String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		usa.setLastUpt(lastUpt);
		this.userSenAssoRepository.save(usa);
		return usa.getUsefulFlag();
	}

	public String toggleMarkSen2(long senId, long userId, long value) {
		// Sentence sen = this.sentenceRepository.findOne(senId);
		UserSenAsso usa = this.userSenAssoRepository.findByUserIdAndSenId(userId, senId);
		if (usa == null) {
			usa = new UserSenAsso();
			usa.setUser(this.userRepository.findOne(userId));
			usa.setSen(this.sentenceRepository.findOne(senId));
			this.userSenAssoRepository.save(usa);
		}
		// String usefulFlag = usa.getUsefulFlag();
		if (value > 0) {
			usa.setUsefulFlag("Yes");
		}
		else {
			usa.setUsefulFlag("No");
		}
		String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		usa.setLastUpt(lastUpt);
		this.userSenAssoRepository.save(usa);

		// initAudio(senId);

		return usa.getUsefulFlag();
	}

	/**
	 * @param senId
	 */
	private void initAudio(long senId) {
		Sentence sen = this.sentenceRepository.findOne(senId);
		String audioPath = sen.getAudioPath();
		String content = sen.getContent();
		if (StringUtils.isBlank(audioPath)) {
			audioPath = MyUtil.genAudioFile(
					ArticleServiceImpl.this.mySettings.getAudioSnippetDir(), content);
			if (StringUtils.isNotBlank(audioPath)) {
				sen.setAudioPath(audioPath);
				sen.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
				ArticleServiceImpl.this.sentenceRepository.save(sen);
			}
		}
	}

	/**
	 * @param senId
	 */
	private void initAudio2(long wordId) {
		Word word = this.wordRepository.findOne(wordId);
		String content = word.getValue();
		String[] contentCharArr = content.split("");
		String contentCharStr = "";
		for (int i = 0; i < contentCharArr.length; i++) {
			contentCharStr += contentCharArr[i] + ", ";
		}
		String content2 = content + " " + contentCharStr + " " + content;
		String audioPath = word.getAudioPath();
		if (StringUtils.isBlank(audioPath)) {
			audioPath = MyUtil.genAudioFile(
					ArticleServiceImpl.this.mySettings.getAudioSnippetDir(), content2);
			if (StringUtils.isNotBlank(audioPath)) {
				word.setAudioPath(audioPath);
				word.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
				ArticleServiceImpl.this.wordRepository.save(word);
			}
		}
	}

	/**
	 * @param id
	 * @param content
	 * @param usefulFlag
	 * @param oper
	 * @param userId
	 */
	public void saveOrUpdateSen(String ids, String content, String usefulFlag,
			String oper, long userId) {
		String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		if ("edit".equals(oper)) {
			Sentence sen = this.sentenceRepository.findOne(Long.parseLong(ids));
			sen.setContent(content);
			List<UserSenAsso> userSenAssoList = sen.getUsers();
			boolean isExistAsso = false;
			for (int i = 0; i < userSenAssoList.size(); i++) {
				UserSenAsso oneUSA = userSenAssoList.get(i);
				User assoUser = oneUSA.getUser();
				if (assoUser != null && assoUser.getId() == userId) {
					oneUSA.setUsefulFlag(usefulFlag);
					isExistAsso = true;
					this.userSenAssoRepository.save(oneUSA);
				}
			}

			if (!isExistAsso) {
				UserSenAsso usa = new UserSenAsso();
				usa.setUser(this.userRepository.findOne(userId));
				usa.setSen(sen);
				usa.setUsefulFlag(usefulFlag);
				usa.setLastUpt(lastUpt);
				userSenAssoList.add(usa);
				sen.setUsers(userSenAssoList);
				this.userSenAssoRepository.save(usa);
			}

			sen.setLastUpt(lastUpt);
			this.sentenceRepository.save(sen);
		}
	}

	/**
	 * @param wordId
	 * @param userId
	 * @param rank
	 */
	public void updateInterest(long wordId, long userId, int rank) {
		UserWordAsso uwa = this.userWordAssoRepository.findByUserIdAndWordId(userId,
				wordId);
		String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		if (uwa != null) {
			// uwa.setRank(rank);
			uwa.setInterest(rank + "");
			uwa.setLastUpt(dateStr);
		}
		else {
			uwa = new UserWordAsso();
			uwa.setWord(this.wordRepository.findOne(wordId));
			uwa.setUser(this.userRepository.findOne(userId));
			// uwa.setRank(rank);
			uwa.setInterest(rank + "");
			uwa.setLastUpt(dateStr);
		}
		this.userWordAssoRepository.save(uwa);

		// initAudio2(wordId);
	}

	public void updateArticleStatus(long articleId, long userId, boolean status) {
		UserArticleForkAsso uafa = this.userArticleForkAssoRepository
				.findByUserIdAndArticleId(userId, articleId);
		if (uafa == null) {
			uafa = new UserArticleForkAsso();
			Article article = new Article();
			article.setId(articleId);
			uafa.setArticle(article);
			User user = new User();
			user.setId(userId);
			uafa.setUser(user);
		}

		uafa.setStatus(status + "");
		String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		uafa.setLastUpt(lastUpt);
		this.userArticleForkAssoRepository.save(uafa);
	}

	/**
	 * @param answerInfo
	 */
	public String submitAnswer(String answerInfo, long userId, long snippetId) {
		List<AudioAnswer> audioAnswerList = this.audioAnswerRepository
				.findByAudioSnippetIdAndAuthorId(snippetId, userId);

		AudioAnswer audioAnswer = null;
		if (!audioAnswerList.isEmpty()) {
			audioAnswer = audioAnswerList.get(0);
		}
		else {
			audioAnswer = new AudioAnswer();
			User user = new User();
			user.setId(userId);
			audioAnswer.setAuthor(user);
			AudioSnippet audioSnippet = new AudioSnippet();
			audioSnippet.setId(snippetId);
			audioAnswer.setAudioSnippet(audioSnippet);
		}
		audioAnswer.setContent(answerInfo);
		audioAnswer.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));

		this.audioAnswerRepository.save(audioAnswer);

		return "submit successfully";
	}

	/**
	 * @param answerInfo
	 */
	public List<AudioAnswer> getAllAnswer(long snippetId) {
		List<AudioAnswer> audioAnswerList = this.audioAnswerRepository
				.findByAudioSnippetIdOrderByRankDesc(snippetId);
		return audioAnswerList;
	}

	/**
	 * @param id
	 * @param name
	 * @param remark
	 * @param oper
	 * @param wordArr2
	 * @param senArr2
	 */
	public void saveOrUpdateQuiz(String id, String name, String remark, String oper,
			String[] wordArr2, String[] senArr2, String[] commArr2, long userId,
			String password) {
		password = StringUtils.trim(password);
		if (StringUtils.isNotEmpty(password)) {
			password = this.passwordEncoder.encodePassword(password, null);
		}
		if ("add".equals(oper)) {
			Quiz quiz = new Quiz();
			quiz.setName(name);
			quiz.setRemark(remark);
			User author = new User();
			author.setId(userId);
			quiz.setAuthor(author);
			quiz.setPassword(password);
			this.quizRepository.save(quiz);

			for (int i = 0; i < wordArr2.length; i++) {
				QuizContent qc = new QuizContent();

				Sentence sentence = new Sentence();
				String oneSenStr = senArr2[i];
				oneSenStr = addSpaceForPunctuate(oneSenStr);
				sentence.setContent(" " + oneSenStr + " ");
				Article article = new Article();
				article.setId(-1L); // DUMMY article
				sentence.setArticle(article);
				this.sentenceRepository.save(sentence);
				qc.setSentence(sentence);

				qc.setComment(StringUtils.trim(commArr2[i]));

				List<Word> wordList = this.wordRepository.findByLowValue(wordArr2[i]
						.toLowerCase());
				Word word = null;
				if (wordList.isEmpty()) {
					word = new Word();
					word.setValue(wordArr2[i]);
					word.setLowValue(wordArr2[i].toLowerCase());
					this.wordRepository.save(word);
				}
				else {
					word = wordList.get(0);
				}
				qc.setWord(word);
				qc.setQuiz(quiz);
				this.quizContentRepository.save(qc);
			}
		}
	}

	/**
	 * @param str
	 * @return
	 */
	private String addSpaceForPunctuate(String str) {
		str = StringUtils.replace(str, ",", " , ");
		str = StringUtils.replace(str, ".", " . ");
		str = StringUtils.replace(str, "!", " ! ");
		str = StringUtils.replace(str, "?", " ? ");
		str = StringUtils.replace(str, "-", " - ");
		return str;
	}

	private String removeSpaceForPunctuate(String str) {
		str = StringUtils.replace(str, " , ", ",");
		str = StringUtils.replace(str, " . ", ".");
		str = StringUtils.replace(str, " ! ", "!");
		str = StringUtils.replace(str, " ? ", "?");
		str = StringUtils.replace(str, " - ", "-");
		return str;
	}

	/**
	 * @param username
	 * @param password
	 * @return
	 */
	public String register(String username, String password) {
		String result = "";
		User user = this.userRepository.findByEmail(username);
		if (user != null) {
			result = "此名称已被注册，请更换";
		}
		else {
			user = new User();
			user.setEmail(username);
			user.setPassword(password);
			String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			user.setLastUpt(dateStr);
			this.userRepository.save(user);

			long dummyId = -1L;
			Article article = this.articleRepository.findOne(dummyId);
			if (article == null) {
				// article = new Article();
				// article.setId(dummyId);
				// article.setName("dummy");
				// this.articleRepository.save(article);

				int updateCount = this.jdbcTemplate.update(
						"insert into article(id, name, delete_flag) value(? , ?, ? )",
						-1, "dummy", 0);
				System.out.println("###updateCount:" + updateCount);
				article = this.articleRepository.findOne(dummyId);

			}
			UserArticleForkAsso uafa = new UserArticleForkAsso();
			uafa.setArticle(article);
			uafa.setUser(user);
			uafa.setLastUpt(dateStr);
			this.userArticleForkAssoRepository.save(uafa);

			result = "注册成功";
		}
		return result;
	}

	/**
	 * @param wordValue
	 * @return
	 */
	public String getWordExplain(String wordValue) {
		String wordExplain = "";
		List<Word> wordList = this.wordRepository.findByLowValue(StringUtils
				.lowerCase(wordValue));
		if (wordList.isEmpty()) {
			wordExplain = "";
		}
		else {
			wordExplain = wordList.get(0).getExplain2();
		}
		return wordExplain;
	}

	public String saveWordExplain(String wordValue, String explainValue) {
		String result = "";
		wordValue = StringUtils.trim(wordValue);
		explainValue = StringUtils.trim(explainValue);
		List<Word> wordList = this.wordRepository.findByLowValue(StringUtils
				.lowerCase(wordValue));
		Word word = null;
		if (wordList.isEmpty()) {
			word = new Word();
			word.setValue(wordValue);
			word.setLowValue(StringUtils.lowerCase(wordValue));
			word.setExplain(explainValue);
			fillWordInfo(word);
			this.wordRepository.save(word);
		}
		else {
			word = wordList.get(0);
			fillWordInfo(word);
			word.setExplain(explainValue);
			this.wordRepository.save(word);
		}
		return result;
	}

	/**
	 * @param wordId
	 * @param userId
	 * @return
	 */
	public String ignoreWord(long wordId, long days, long userId) {
		// List<QuizResult> qrList =
		// this.quizResultRepository.findByUserIdAndWordId(userId,
		// wordId);
		// for (QuizResult qr : qrList) {
		// qr.setIsRight(1);
		// this.quizResultRepository.save(qr);
		// }

		UserWordAsso uwa = this.userWordAssoRepository.findByUserIdAndWordId(userId,
				wordId);

		if (uwa == null) {
			uwa = new UserWordAsso();
			User user = new User();
			user.setId(userId);
			uwa.setUser(user);
			Word dbWord = this.wordRepository.findOne(wordId);
			uwa.setWord(dbWord);
		}
		if (days > 365) {
			uwa.setRank(-1);
		}

		Calendar currCal = Calendar.getInstance();
		currCal.add(Calendar.DATE, (int) days);
		String effectDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currCal
				.getTime());
		uwa.setEffectDate(effectDateStr);
		uwa.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		this.userWordAssoRepository.save(uwa);

		return "done";

	}

	/**
	 * @param wxUser
	 */
	public User updateUserInDB(WeiXinUser wxUser) {
		User user = this.userRepository.findByOpenId(wxUser.getOpenId());
		if (user == null) {
			user = new User();
		}
		user.setNickname(wxUser.getNickname());
		user.setSex(wxUser.getSex());
		user.setProvince(wxUser.getProvince());
		user.setCity(wxUser.getCity());
		user.setHeadimgurl(wxUser.getHeadimgurl());
		user.setOpenId(wxUser.getOpenId());
		if (StringUtils.isBlank(user.getWxRemiderHour())) {
			user.setWxRemiderHour("19");
		}
		this.userRepository.save(user);
		return user;
	}

	public void sendMsg(String accessToken) {
		ClassLoader classLoader = getClass().getClassLoader();
		File jsonTemplateFile = new File(classLoader.getResource(
				"dailyNotifyTemplate.json").getFile());
		System.out.println("jsonTemplateFile:" + jsonTemplateFile.getAbsolutePath());

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		// DefaultHttpClient httpClient = new DefaultHttpClient();
		String templateJSON;
		try {
			templateJSON = FileUtils.readFileToString(jsonTemplateFile);
			System.out.println(templateJSON);
			Iterable<User> userIt = this.userRepository.findAll();
			int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			for (User user : userIt) {
				if ((currHour + "").equals(user.getWxRemiderHour())) {
					String userOpenId = user.getOpenId();
					if (StringUtils.isNotEmpty(userOpenId)) {
						System.out.println("###begin send to " + user.getNickname()
								+ " new Date:" + new Date());
						String userJSON = templateJSON.replace("REPLACE_TO_USER",
								userOpenId);
						// String originalURI = MyFilter.basePath
						// + "html/ajax/ajax.html#page/freeQuiz";
						String basePath = StringUtils.isBlank(MyFilter.basePath) ? "http://english.tiger.mopaas.com/"
								: MyFilter.basePath;
						String originalURI = basePath + "html/ajax/ajaxMyFreeQuiz.html";
						System.out.println("originalURI:" + originalURI);
						String oauth2URL = "https://open.weixin.qq.com/connect/oauth2/authorize"
								+ "?appid=wxad1e1211d18cd79d"
								+ "&redirect_uri="
								+ URLEncoder.encode(originalURI, "UTF-8")
								+ "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
						System.out.println("oauth2URL:" + oauth2URL);
						userJSON = userJSON.replace("REPLACE_URL", oauth2URL);
						userJSON = userJSON.replace("REPLACE_TITLE", "亲，背单词了");
						userJSON = userJSON.replace("REPLACE_GOODS", "单词日报");
						userJSON = userJSON.replace("REPLACE_PRICE", "免费");
						userJSON = userJSON.replace("REPLACE_DATE", new SimpleDateFormat(
								"yyyy-MM-dd").format(new Date()));
						userJSON = userJSON.replace("REPLACE_REMARK", "日拱一卒，点击查看详情");
						System.out.println("userJSON:" + userJSON);
						StringEntity requestEntity = new StringEntity(userJSON,
								ContentType.APPLICATION_JSON);
						String uri = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="
								+ accessToken;
						HttpPost method = new HttpPost(uri);
						method.setEntity(requestEntity);
						HttpResponse result = closeableHttpClient.execute(method);
						String resData = EntityUtils.toString(result.getEntity());
						JSONObject json = (JSONObject) new JSONParser().parse(resData);
						System.out.println("errcode:" + json.get("errcode"));
						System.out.println("errmsg:" + json.get("errmsg"));
						System.out.println("msgid:" + json.get("msgid"));
					}

				}
			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param userId
	 * @param articleId
	 * @param bookMarkIndex
	 */
	public void setBookMark(long userId, long articleId, int bookMarkIndex, String type) {
		UserArticleForkAsso uafa = this.userArticleForkAssoRepository
				.findByUserIdAndArticleId(userId, articleId);
		if (uafa == null) {
			uafa = new UserArticleForkAsso();
			Article article = new Article();
			article.setId(articleId);
			uafa.setArticle(article);
			User user = new User();
			user.setId(userId);
			uafa.setUser(user);
		}
		if ("word".equals(type)) {
			uafa.setBookMarkIndex(bookMarkIndex);
		}
		else {
			uafa.setBookMarkIndexForSen(bookMarkIndex);
		}
		uafa.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		this.userArticleForkAssoRepository.save(uafa);
	}

	/**
	 * @param userId
	 * @param articleId
	 * @return
	 */
	public int getBookMark(long userId, long articleId, String type) {
		int bookMarkIndex = 0;
		UserArticleForkAsso uafa = this.userArticleForkAssoRepository
				.findByUserIdAndArticleId(userId, articleId);
		if (uafa != null) {
			if ("word".equals(type)) {
				bookMarkIndex = uafa.getBookMarkIndex();
			}
			else {
				bookMarkIndex = uafa.getBookMarkIndexForSen();
			}
		}
		return bookMarkIndex;
	}

	/**
	 *
	 */
	public void initWork() {
		// long dummyId = -1L;
		// Article article = this.articleRepository.findOne(dummyId);
		// if (article == null) {
		// article = new Article();
		// article.setId(dummyId);
		// article.setName("dummy");
		// this.articleRepository.save(article);
		// }
	}

	public String getJSAPITicket(String accessToken) {
		WebDriver driver = new HtmlUnitDriver();

		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
				+ accessToken + "&type=jsapi";
		driver.get(url);
		String pageSource = driver.getPageSource();
		try {
			pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
		System.out.println("pageSource:" + pageSource);

		// {"errcode":0,"errmsg":"ok","ticket":"sM4AOVdWfPE4DxkXGEs8VKKVQl3wRLx9tZtPKOwlUSCE5nvVJtpMR0QjmpKagP8p-cR2ym4bQ6FUh5swvTk0xw",
		// "expires_in":7200}

		JSONObject json = null;
		try {
			json = (JSONObject) new JSONParser().parse(pageSource);
		}
		catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
		System.out.println("errcode:" + json.get("errcode"));
		System.out.println("errmsg:" + json.get("errmsg"));
		String jsapiTicket = (String) json.get("ticket");
		System.out.println("ticket:" + jsapiTicket);
		System.out.println("expires_in:" + json.get("expires_in"));
		return jsapiTicket;
	}

	public String getAccessToken() {
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
		catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
		return accessToken;
	}

	/**
	 * @return
	 */
	public String[] getWXShareInfo(String shareUrl) {
		String accessToken = getAccessToken();
		String appId = "wxad1e1211d18cd79d";
		String jsapiTicket = getJSAPITicket(accessToken);
		Map<String, String> signMapResult = Sign.sign(jsapiTicket, shareUrl);
		String timestamp = signMapResult.get("timestamp");
		String nonceStr = signMapResult.get("nonceStr");
		String signature = signMapResult.get("signature");
		return new String[] { appId, timestamp, nonceStr, signature };
	}

	public void initWordAudio() {
		String currDateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		System.out.println("currDateStr:" + currDateStr);
		// List<WordSummary> wordList = null;
		String sql = "SELECT distinct w.id, w.explain2, w.value, w.pron, w.level, w.audio_path, uwa2.last_upt2, uwa2.effect_date "
				+ " from word w , ( "
				+ " select word_id, max(last_upt) last_upt2 , effect_date from user_word_asso "
				+ " where interest = 1 and (effect_date is null or effect_date <= ? or effect_date > '3000') "
				+ "  group by word_id " + " ) uwa2 where w.id = uwa2.word_id   ";

		System.out.println("sql:" + sql);
		// System.out.println("begin getWordListFromQuizResult " + new Date());
		List<WordBean2> results = this.jdbcTemplate.query(sql,
				new Object[] { currDateStr }, new RowMapper<WordBean2>() {
					@Override
					public WordBean2 mapRow(ResultSet rs, int rowNum) throws SQLException {
						String audioPath = rs.getString("audio_path");
						String content = rs.getString("value");
						String[] contentCharArr = content.split("");
						String contentCharStr = "";
						for (int i = 0; i < contentCharArr.length; i++) {
							contentCharStr += contentCharArr[i] + ", ";
						}
						String content2 = content + " " + contentCharStr + " " + content;
						if (StringUtils.isBlank(audioPath)) {
							audioPath = MyUtil.genAudioFile(
									ArticleServiceImpl.this.mySettings
											.getAudioSnippetDir(), content2);
						}

						long wordId = rs.getLong("id");
						if (StringUtils.isNotBlank(audioPath)) {
							Word word = ArticleServiceImpl.this.wordRepository
									.findOne(wordId);
							word.setAudioPath(audioPath);
							word.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(new Date()));
							ArticleServiceImpl.this.wordRepository.save(word);
						}
						return new WordBean2(wordId, content, rs.getString("explain2"),
								rs.getString("pron"), audioPath, rs
										.getString("last_upt2"));

					}
				});

	}

	public void initSenAudio() {
		String audioDirStr = this.mySettings.getAudioSnippetDir();
		System.out.println("mySettings.getAudioDir():" + audioDirStr);

		String sql = "SELECT s.id, s.content, s.last_upt, s.audio_path FROM sentence s, user_sen_asso usa "
				+ " where s.id = usa.sen_id " + " and usa.useful_flag = 'Yes' ";

		System.out.println("sql:" + sql);
		List<SenBean> results = this.jdbcTemplate.query(sql, new RowMapper<SenBean>() {
			@Override
			public SenBean mapRow(ResultSet rs, int rowNum) throws SQLException {
				String audioPath = rs.getString("audio_path");
				String content = rs.getString("content");
				if (StringUtils.isBlank(audioPath)) {
					audioPath = MyUtil.genAudioFile(
							ArticleServiceImpl.this.mySettings.getAudioSnippetDir(),
							content);
				}
				long senId = rs.getLong("id");
				if (StringUtils.isNotBlank(audioPath)) {
					Sentence senDB = ArticleServiceImpl.this.sentenceRepository
							.findOne(senId);
					senDB.setAudioPath(audioPath);
					senDB.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()));
					ArticleServiceImpl.this.sentenceRepository.save(senDB);
				}
				return new SenBean(senId, content, "", audioPath, rs
						.getString("last_upt"));

			}
		});
	}

	/**
	 * @param from
	 * @param to
	 * @param newTo
	 * @return
	 */
	public String splitAudio(long snippetId, int from, int to, int newTo) {
		AudioSnippet audioSnippet = this.audioSnippetRepository.findOne(snippetId);
		audioSnippet.setEnd(newTo);
		AudioSnippet audioSnippet2 = new AudioSnippet();
		audioSnippet2.setStart(newTo);
		audioSnippet2.setEnd(to);
		Audio audio = audioSnippet.getAudio();
		audioSnippet2.setAudio(audio);

		File audioFile = new File(this.mySettings.getAudioDir() + File.separator
				+ audio.getUrl());
		Mp3Fenge helper = new Mp3Fenge(audioFile);
		String audio1FileName = "seg_" + System.currentTimeMillis() + "_"
				+ new Random().nextInt(10000) + ".mp3";
		helper.generateNewMp3ByTime(new File(this.mySettings.getAudioDir(),
				audio1FileName), from * 1000, newTo * 1000);

		String audio2FileName = "seg_" + System.currentTimeMillis() + "_"
				+ new Random().nextInt(10000) + ".mp3";
		helper.generateNewMp3ByTime(new File(this.mySettings.getAudioDir(),
				audio2FileName), newTo * 1000, to * 1000);

		audioSnippet.setUrl(audio1FileName);
		audioSnippet2.setUrl(audio2FileName);

		this.audioSnippetRepository.save(audioSnippet);
		this.audioSnippetRepository.save(audioSnippet2);
		return audioSnippet2.getId() + "";
	}

	/**
	 * @param audio
	 */
	public void addAudio(Audio audio) {
		this.audioRepository.save(audio);
		AudioSnippet audioSnippet = new AudioSnippet();
		audioSnippet.setAudio(audio);
		audioSnippet.setStart(0);
		audioSnippet.setEnd(audio.getDuration());
		this.audioSnippetRepository.save(audioSnippet);
	}

	/**
	 * @param snippetId
	 * @param userId
	 * @return
	 */
	public String getMyAnswer(long snippetId, long userId) {
		String answer = "";
		List<AudioAnswer> audioAnswerList = this.audioAnswerRepository
				.findByAudioSnippetIdAndAuthorId(snippetId, userId);

		AudioAnswer audioAnswer = null;
		if (!audioAnswerList.isEmpty()) {
			audioAnswer = audioAnswerList.get(0);
			answer = audioAnswer.getContent();
		}
		return answer;
	}
}
