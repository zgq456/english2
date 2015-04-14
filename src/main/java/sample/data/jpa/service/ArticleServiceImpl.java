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
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import sample.data.jpa.domain.Article;
import sample.data.jpa.domain.ArticleWordAsso;
import sample.data.jpa.domain.Quiz;
import sample.data.jpa.domain.QuizContent;
import sample.data.jpa.domain.QuizResult;
import sample.data.jpa.domain.QuizWordBean;
import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.User;
import sample.data.jpa.domain.UserArticleForkAsso;
import sample.data.jpa.domain.UserSenAsso;
import sample.data.jpa.domain.UserWordAsso;
import sample.data.jpa.domain.Word;
import sample.data.jpa.repository.ArticleRepository;
import sample.data.jpa.repository.ArticleWordAssoRepository;
import sample.data.jpa.repository.QuizContentRepository;
import sample.data.jpa.repository.QuizRepository;
import sample.data.jpa.repository.QuizResultRepository;
import sample.data.jpa.repository.SentenceRepository;
import sample.data.jpa.repository.UserArticleForkAssoRepository;
import sample.data.jpa.repository.UserRepository;
import sample.data.jpa.repository.UserSenAssoRepository;
import sample.data.jpa.repository.UserWordAssoRepository;
import sample.data.jpa.repository.WordRepository;
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
	UserArticleForkAssoRepository userArticleForkAssoRepository;
	@Autowired
	QuizRepository quizRepository;
	@Autowired
	QuizContentRepository quizContentRepository;
	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	QuizResultRepository quizResultRepository;

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

	/**
	 * @param id
	 * @return
	 */
	public List<QuizWordBean> getWordListForQuiz2(long quizId) {
		List<QuizContent> quizContentList = this.quizContentRepository
				.findByQuizId(quizId);
		List<QuizWordBean> results = new ArrayList<QuizWordBean>();
		for (int i = 0; i < quizContentList.size(); i++) {
			QuizContent qc = quizContentList.get(i);
			QuizWordBean qwb = new QuizWordBean(qc.getWord().getId(), qc.getWord()
					.getValue(), qc.getWord().getExplain2(), 5, qc.getSentence()
					.getContent(), qc.getWord().getMark());
			results.add(qwb);
		}

		long userId = 0L;
		generateQuestion(userId, results, MyConstants.CUSTOM_QUIZ);
		return results;
	}

	public List<QuizWordBean> getWordListForQuiz(long userId) {
		String sql = "SELECT word_id, value,  explain2, uwa.rank, w.mark  FROM user_word_asso uwa, word w, user u "
				+ " where uwa.word_id = w.id and u.id = uwa.user_id and u.id = ? " // and
																					// w.mark
																					// > 0
				+ " order by rank desc, mark asc limit 0, 50";
		System.out.println("begin getWordListForQuiz " + new Date());
		List<QuizWordBean> results = this.jdbcTemplate.query(sql,
				new Object[] { userId }, new RowMapper<QuizWordBean>() {
					@Override
					public QuizWordBean mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return new QuizWordBean(rs.getLong("word_id"), rs
								.getString("value"), rs.getString("explain2"), rs
								.getInt("rank"), "", rs.getInt("mark"));
					}
				});

		System.out.println("begin generateQuestion " + new Date());
		generateQuestion(userId, results, MyConstants.FREE_QUIZ);

		return results.size() > 10 ? results.subList(0, 10) : results; // FIXME
	}

	private void generateQuestion(long userId, List<QuizWordBean> results, int mode) {
		for (int i = 0; i < results.size(); i++) {
			QuizWordBean qwb = results.get(i);
			Word word = this.wordRepository.findOne(qwb.getWordId());
			List<Sentence> senList = new ArrayList<Sentence>();
			if (mode == MyConstants.FREE_QUIZ) {
				senList = this.sentenceRepository.findByArticleUserIdSubGrid(userId, "% "
						+ word.getValue().toLowerCase() + " %");
			}
			int blankCount = 0;
			String answer = "";
			String wordExplain = word.getExplain2();
			if (word.getValue().length() <= 2
					|| (mode == MyConstants.FREE_QUIZ && senList.isEmpty())
					|| ("UNKNOWN".equals(wordExplain) || StringUtils.isEmpty(wordExplain))) {
				results.remove(i);
				i--;
			}
			else {

				String sentence = null;
				if (mode == MyConstants.FREE_QUIZ) {
					sentence = senList.get(0).getContent();
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
									+ i
									+ "' type='input' class='input-answer' onKeyUp='checkAnswer("
									+ i + ", " + blankCount + ")' /> " + "ing" + " ";
						}
						else if (words[j].toLowerCase().endsWith("es")) {
							answer = words[j].substring(0, words[j].length() - 2);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ i
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ i + ", " + blankCount + ")' /> " + "es" + " ";
						}
						else if (words[j].toLowerCase().endsWith("s")) {
							answer = words[j].substring(0, words[j].length() - 1);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ i
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ i + ", " + blankCount + ")' /> " + "s" + " ";
						}
						else if (words[j].toLowerCase().endsWith("ed")) {
							answer = words[j].substring(0, words[j].length() - 2);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ i
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ i + ", " + blankCount + ")' /> " + "ed" + " ";
						}
						else if (words[j].toLowerCase().endsWith("d")) {
							answer = words[j].substring(0, words[j].length() - 1);
							blankCount = answer.length();
							sentence2 += "<input id='input"
									+ i
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ i + ", " + blankCount + ")' /> " + "d" + " ";
						}
						else { // if (qwb.getMark() == MyConstants.ORIGINAL_TENSE)
							answer = words[j].substring(1);
							blankCount = answer.length();
							sentence2 += words[j].substring(0, 1)
									+ "<input id='input"
									+ i
									+ "' type='input'  class='input-answer' onKeyUp='checkAnswer("
									+ i + ", " + blankCount + ")' /> ";
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
		System.out.println("###name:" + articleName);
		// String filePath = "readme.txt";
		File uploadDir = new File("uploadFiles");
		File articlePath = new File(uploadDir + File.separator + userId, articleName);
		InputStream stream = null;
		try {
			String content = null;
			Tika tika = new Tika();
			if ("doc".equals(type)) {
				stream = new FileInputStream(articlePath);
				content = tika.parseToString(stream);
			}
			else if ("webpage".equals(type)) {
				HtmlUnitDriver driver = new HtmlUnitDriver();
				driver.get(article.getUrl());
				String pageSource = driver.getPageSource();
				driver.close();
				File tempFile = new File("uploadFiles/tmp/" + System.currentTimeMillis()
						+ ".txt");
				FileUtils.writeStringToFile(tempFile, pageSource);
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
			content = content.replace("¡¯", "'");
			System.out.println("###content:" + content);

			File tempFile = new File(uploadDir, System.currentTimeMillis() + ".txt");
			FileUtils.writeStringToFile(tempFile, content);
			// option #1: By sentence.
			DocumentPreprocessor dp = new DocumentPreprocessor(tempFile.getAbsolutePath());
			final Set<Sentence> sentencesSet = new HashSet<Sentence>();
			final List<ArticleWordAsso> awaList = new ArrayList<ArticleWordAsso>();
			System.out.println("begin parse sentence");
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
			System.out.println("begin save  ArticleWordAsso");
			for (ArticleWordAsso awa : awaList) {
				this.articleWordAssoRepository.save(awa);
			}

			System.out.println("###parse done");
			article.setSentences(sentencesSet);
			this.articleRepository.save(article);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
		finally {
			IOUtils.closeQuietly(stream);
		}

	}

	private void parseSentence(Article article, long userId, Set<Sentence> sentencesSet,
			List sentence, List<ArticleWordAsso> awaList) {
		System.out.println("###sentence:" + sentence);
		String sentenceStr = "";
		for (int i = 0; i < sentence.size(); i++) {
			Object oneWord = sentence.get(i);
			sentenceStr += (oneWord + " ");

			String lowCaseWord = oneWord.toString().toLowerCase();
			// TODO MARK WORD -1 = ignore
			if ("...".equals(lowCaseWord) || ".".equals(lowCaseWord)
					|| ",".equals(lowCaseWord) || "â–&nbsp;".equals(lowCaseWord)
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
					this.wordRepository.save(dbWord);
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

		}
		Sentence oneSentence = new Sentence();
		oneSentence.setContent(" " + sentenceStr + " ");
		oneSentence.setArticle(article);
		oneSentence.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		this.sentenceRepository.save(oneSentence);
		sentencesSet.add(oneSentence);
	}

	public void test() {
		Article article = this.articleRepository.findOne(6L);
		for (Sentence oldSen : article.getSentences()) {
			System.out.println("###remove oldSen:" + oldSen.getId());
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
				System.out.println("###remove oldSen:" + oldSen.getId());
				this.userSenAssoRepository.deleteBySenId(oldSen.getId());
				this.sentenceRepository.delete(oldSen.getId());
			}
		}

		List<ArticleWordAsso> words = article.getWords();
		if (words != null) {
			for (ArticleWordAsso oldAwa : words) {
				System.out.println("###remove oldAwa:" + oldAwa);
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

	public JqGridData<Quiz> getQuizList(int rows, int currPage, String sidx, String sord) {
		if (StringUtils.isEmpty(sidx)) {
			sidx = "id";
			sord = "desc";
		}
		Pageable pageable = new PageRequest(currPage - 1, rows,
				"asc".equals(sord) ? Direction.ASC : Direction.DESC, sidx);
		List<Quiz> quizList = this.quizRepository.findAll(pageable).getContent();
		long totalCount = this.quizRepository.count();
		long totalNumberOfRecords = totalCount;
		long totalNumberOfPages = (totalCount % rows == 0 ? (totalCount / rows)
				: (totalCount / rows + 1));

		JqGridData<Quiz> gridData = new JqGridData<Quiz>(totalNumberOfPages, currPage,
				totalNumberOfRecords, quizList);
		return gridData;
	}

	public Quiz getQuiz(long qId) {
		return this.quizRepository.findOne(qId);
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

		for (int i = 0; i < wordList.size(); i++) {
			Word word = wordList.get(i);
			// word.setTempRank(parseRank(word.getTempRank()));
		}

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
				removeOldData(articleId);
				this.articleRepository.delete(articleId);
			}
		}
		else if ("add".equals(oper)) {
			User user = this.userRepository.findOne(userId);
			article.setUser(user);
			String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			article.setLastUpt(lastUpt);

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
	public void updateRank(long wordId, long userId, int rank) {
		UserWordAsso uwa = this.userWordAssoRepository.findByUserIdAndWordId(userId,
				wordId);
		String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		if (uwa != null) {
			uwa.setRank(rank);
			uwa.setLastUpt(dateStr);
		}
		else {
			uwa = new UserWordAsso();
			uwa.setWord(this.wordRepository.findOne(wordId));
			uwa.setUser(this.userRepository.findOne(userId));
			uwa.setRank(rank);
			uwa.setLastUpt(dateStr);
		}
		this.userWordAssoRepository.save(uwa);
	}

	/**
	 * @param answerInfo
	 */
	public void submitAnswer(String answerInfo, long userId, long qId) {
		Quiz quiz = null;
		if (qId != 0) {
			this.quizResultRepository.deleteByUserIdAndQuizId(userId, qId);
			quiz = this.quizRepository.findOne(qId);
			quiz.setHot(quiz.getHot() + 1);
		}
		String[] answers = StringUtils.split(answerInfo, ",");
		for (int i = 0; i < answers.length; i++) {
			String[] oneAnswerArr = answers[i].split(":");
			long wordId = Long.parseLong(oneAnswerArr[0]);
			int isRight = Integer.parseInt(oneAnswerArr[1]);
			QuizResult qr = new QuizResult();

			User user = new User();
			user.setId(userId);
			qr.setUser(user);
			Word word = new Word();
			word.setId(wordId);
			qr.setWord(word);
			qr.setIsRight(isRight);
			String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());
			qr.setLastUpt(dateStr);
			if (quiz != null) {
				qr.setQuiz(quiz);
			}
			this.quizResultRepository.save(qr);
		}

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
			String[] wordArr2, String[] senArr2, long userId) {
		if ("add".equals(oper)) {
			Quiz quiz = new Quiz();
			quiz.setName(name);
			quiz.setRemark(remark);
			User author = new User();
			author.setId(userId);
			quiz.setAuthor(author);
			this.quizRepository.save(quiz);

			for (int i = 0; i < wordArr2.length; i++) {
				QuizContent qc = new QuizContent();

				Sentence sentence = new Sentence();
				sentence.setContent(senArr2[i]);
				Article article = new Article();
				article.setId(-1L); // DUMMY article
				sentence.setArticle(article);
				this.sentenceRepository.save(sentence);
				qc.setSentence(sentence);

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
	 * @param username
	 * @param password
	 * @return
	 */
	public String register(String username, String password) {
		String result = "";
		User user = this.userRepository.findByEmail(username);
		if (user != null) {
			result = "此用户名已被注册，请更换";
		}
		else {
			user = new User();
			user.setEmail(username);
			user.setPassword(password);
			this.userRepository.save(user);
			result = "注册成功";
		}
		return result;
	}
}
