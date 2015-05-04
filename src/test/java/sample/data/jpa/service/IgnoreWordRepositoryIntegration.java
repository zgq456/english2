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

//import org.apache.tika.Tika;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.Transactional;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import sample.data.jpa.SampleDataJpaApplication;
import sample.data.jpa.domain.Article;
import sample.data.jpa.domain.Quiz;
import sample.data.jpa.domain.QuizContent;
import sample.data.jpa.domain.QuizRating;
import sample.data.jpa.domain.QuizWordBean;
import sample.data.jpa.domain.SenBean;
import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.User;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.process.CoreLabelTokenFactory;
//import edu.stanford.nlp.process.DocumentPreprocessor;
//import edu.stanford.nlp.process.PTBTokenizer;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.process.CoreLabelTokenFactory;
//import edu.stanford.nlp.process.DocumentPreprocessor;
//import edu.stanford.nlp.process.PTBTokenizer;
import sample.data.jpa.domain.Word;
import sample.data.jpa.domain.WordRelation;
import sample.data.jpa.domain.WordSummary;
import sample.data.jpa.repository.ArticleRepository;
import sample.data.jpa.repository.ArticleWordAssoRepository;
import sample.data.jpa.repository.GroupRepository;
import sample.data.jpa.repository.HotelRepository;
import sample.data.jpa.repository.QuizContentRepository;
import sample.data.jpa.repository.QuizRepository;
import sample.data.jpa.repository.QuizResultRepository;
import sample.data.jpa.repository.SentenceRepository;
import sample.data.jpa.repository.UserRepository;
import sample.data.jpa.repository.WordRelationRepository;
import sample.data.jpa.repository.WordRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * Integration tests for {@link HotelRepository}.
 *
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleDataJpaApplication.class)
public class IgnoreWordRepositoryIntegration {

	@Autowired
	WordRepository wordRepository;

	@Autowired
	QuizResultRepository quizResultRepository;
	@Autowired
	ArticleWordAssoRepository articleWordAssoRepository;
	@Autowired
	WordRelationRepository wordRelationRepository;
	@Autowired
	ArticleRepository articleRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	SentenceRepository sentenceRepository;
	@Autowired
	GroupRepository groupRepository;
	@Autowired
	QuizRepository quizRepository;
	@Autowired
	QuizContentRepository quizContentRepository;

	@Autowired
	ArticleServiceImpl articleService;

	@Test
	public void getWords() {
		WebDriver driver = new HtmlUnitDriver();

		for (int p = 1; p <= 24; p++) {
			String urlSuffix = p < 10 ? ("0" + p) : ("" + p);

			// String url =
			// "http://www.manythings.org/vocabulary/lists/l/words.php?f=3esl.01";
			String url = "";

			url = "";
			url = "http://www.manythings.org/vocabulary/lists/l/words.php?f=3esl."
					+ urlSuffix;

			driver.get(url);
			// String pageSource = driver.getPageSource();
			WebElement divElm = driver.findElement(By.className("wrapco"));
			// String wordStr = divElm.getText();
			// System.out.println(wordStr);
			List<WebElement> lis = divElm.findElements(By.tagName("li"));
			for (int i = 0; i < lis.size(); i++) {
				String wordVal = lis.get(i).getText();
				wordVal = StringUtils.trim(wordVal);
				System.out.println((i + 1) + ":" + wordVal);
				if (StringUtils.isNotBlank(wordVal)) {
					Word word = new Word();
					word.setValue(wordVal);
					word.setLowValue(wordVal.toLowerCase());
					word.setMark(MyConstants.ORIGINAL_TENSE);
					this.wordRepository.save(word);
				}
			}
		}
	}

	@Test
	public void getAllLevel() throws Exception {
		ExecutorService exec = Executors.newFixedThreadPool(30);
		Iterable<Word> wordIt = this.wordRepository.findAll();
		for (Word word0 : wordIt) {
			final Word word = word0;
			exec.execute(new Runnable() {
				@Override
				public void run() {
					if (word.getLevel() == null) {
						String wordVal = word.getValue();
						// level, explain, pronStr
						Object[] objArr = getLevel(wordVal);
						int level = Integer.parseInt(objArr[0].toString());
						String explain = objArr[1].toString();
						String pronStr = objArr[2].toString();
						System.out.println("wordVal:" + wordVal + " level:" + level
								+ " explain:" + explain + " pronStr:" + pronStr);
						word.setLevel(level);
						word.setExplain2(explain);
						word.setPron(pronStr);
						word.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date()));
						IgnoreWordRepositoryIntegration.this.wordRepository.save(word);
						// System.out.println(word.getExplain2());
					}
				}
			});

		}
		exec.shutdown();
		while (!exec.isTerminated()) {
			System.out.println("slepp");
			Thread.sleep(10000);
		}
	}

	public Object[] getLevel(String wordVal) {
		WebDriver driver = new HtmlUnitDriver();
		driver.get("http://dict.cn/" + wordVal);
		String pageSource = driver.getPageSource();
		int level = 0;
		if (pageSource.contains("level_")) {
			List<WebElement> allAList = driver.findElement(By.className("word-cont"))
					.findElements(By.tagName("a"));
			for (int i = 0; i < allAList.size(); i++) {
				String levelStr = allAList.get(i).getAttribute("class");
				if (StringUtils.startsWith(levelStr, "level_")) {
					level = Integer.parseInt(levelStr.replace("level_", ""));
					break;
				}
			}
		}
		System.out.println("level:" + level);

		String pronStr = "";
		// System.out.println(pageSource);
		String explain = "";
		if (pageSource.contains("æ‚¨è¦æŸ¥æ‰¾çš„æ˜¯ä¸æ˜¯")) {
			explain = "UNKNOWN";
		}
		else {
			WebElement div = null;
			try {
				div = driver.findElement(By.xpath("//div[@class='basic clearfix']"));
				explain = div.getText();
			}
			catch (Exception e) {
				e.printStackTrace();
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

		return new Object[] { level, explain, pronStr };
	}

	@Test
	public void enhanceWord() {
		this.articleService.enhanceWord();
	}

	public Map<String, Word> getAllWordsMap() {
		Map<String, Word> allWordMap = new HashMap<String, Word>();
		Iterable<Word> wordIt = this.wordRepository.findAll();
		for (Word word : wordIt) {
			allWordMap.put(word.getLowValue(), word);
		}
		System.out.println("###allWordMap:" + allWordMap);
		return allWordMap;
	}

	@Test
	public void getIrreWords() {
		Map<String, Word> allWordMap = getAllWordsMap();
		WebDriver driver = new HtmlUnitDriver();
		driver.get("http://www.usingenglish.com/reference/irregular-verbs/");
		WebElement table = driver.findElement(By.className("irregverbs"));
		List<WebElement> trList = table.findElements(By.tagName("tr"));
		int trSize = trList.size();
		System.out.println("###trSize:" + trSize);
		for (int i = 0; i < trSize; i++) {
			System.out.println("###oneTR");
			List<WebElement> tdList = trList.get(i).findElements(By.tagName("td"));

			int tdSize = tdList.size();
			if (tdSize > 0) {
				String orginal = tdList.get(0).getText();
				Word orginalWord = allWordMap.get(orginal.toLowerCase());
				if (orginalWord == null) {
					// throw new RuntimeException("orginal:" + orginal
					// + " cannot get its source");
					String wordVal = orginal;
					Word word = new Word();
					word.setValue(wordVal);
					word.setLowValue(wordVal.toLowerCase());
					word.setMark(MyConstants.ORIGINAL_TENSE);
					word.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()));
					this.wordRepository.save(word);
					allWordMap.put(wordVal.toLowerCase(), word);
					orginalWord = allWordMap.get(orginal.toLowerCase());
				}
				System.out.println("###orginalWord:" + orginalWord);
				for (int j = 1; j < tdSize; j++) {
					String oneTenseArr = tdList.get(j).getText();
					for (int k = 0; k < oneTenseArr.split("/").length; k++) {
						String oneTense = oneTenseArr.split("/")[k];
						System.out.println("###oneTense:" + oneTense);
						List<Word> tenseWordList = this.wordRepository
								.findByLowValue(oneTense.toLowerCase());
						Word oneTenseWord = tenseWordList.isEmpty() ? null
								: tenseWordList.get(0);
						if (oneTenseWord == null) {
							Word word = new Word();
							word.setValue(oneTense);
							word.setLowValue(oneTense.toLowerCase());
							word.setMark(MyConstants.NOT_SET);
							word.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(new Date()));
							this.wordRepository.save(word);
							allWordMap.put(oneTense.toLowerCase(), word);
							oneTenseWord = allWordMap.get(oneTense.toLowerCase());
						}
						List<WordRelation> wrList = this.wordRelationRepository
								.findByWord1IdAndWord2Id(orginalWord.getId(),
										oneTenseWord.getId());
						if (wrList.isEmpty()) {
							WordRelation wr = new WordRelation();
							wr.setWord1(orginalWord);
							wr.setWord2(oneTenseWord);
							wr.setRelation(1);
							this.wordRelationRepository.save(wr);
						}
						System.out.print(oneTense + " ### ");
						// if(this.wordRepository.find)
					}
				}
			}
			System.out.println();
		}
	}

	@Test
	public void getWordBySearch() {
		// List<Word> wordList = this.wordRepository.findByLowValue("fed");
		// System.out.println("###wordList:" + wordList);

		// List<WordRelation> wrList =
		// this.wordRelationRepository.findByWord1IdAndWord2Id(
		// 21840L, 44036L);
		// System.out.println("###wrList:" + wrList.size());

		// Group group1 = new Group();
		// group1.setName("level one");
		// this.groupRepository.save(group1);
		// Group group2 = new Group();
		// group2.setName("level two");
		// List<Word> words = new ArrayList<Word>();
		// words.add(this.wordRepository.findOne(21886L));
		// words.add(this.wordRepository.findOne(21887L));
		// group2.setWords(words);
		// this.groupRepository.save(group2);

	}

	@Test
	public void parseFile() throws Exception {
		String filePath = "F:/research/2014/fcm/player/lib/Camel in Action.pdf";
		// String filePath = "http://tika.apache.org/1.7/examples.html";
		// String filePath = "readme.txt";
		InputStream stream = new FileInputStream(filePath);
		Tika tika = new Tika();
		try {
			String content = tika.parseToString(new URL(filePath));
			// String content = tika.parseToString(stream);
			System.out.println("###content:" + content);
		}
		finally {
			stream.close();
		}

	}

	@Test
	public void parseExample() throws IOException, SAXException, TikaException {
		// InputStream stream = IgnoreWordRepositoryIntegration.class
		// .getResourceAsStream("http://tika.apache.org/1.7/examples.html");
		// String filePath = "F:/research/2014/fcm/player/lib/Camel in Action.pdf";
		String filePath = "c:/test.txt";
		InputStream stream = new FileInputStream(filePath);
		AutoDetectParser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		try {
			parser.parse(stream, handler, metadata);
			String content = handler.toString();
			System.out.println("content:" + content);
		}
		finally {
			stream.close();
		}
	}

	@Test
	public void test() throws Exception {

		System.out.println("###test");

		Article article = this.articleRepository.findOne(3L);
		// List<ArticleWordAsso> words = new ArrayList<ArticleWordAsso>();
		// ArticleWordAsso awa = new ArticleWordAsso();
		// awa.setArticle(article);
		// awa.setWord(this.wordRepository.findOne(1L));
		// this.articleWordAssoRepository.save(awa);
		// words.add(awa);
		// article.setWords(words);
		// this.articleRepository.save(article);
		// for (Sentence oldSen : article.getSentences()) {
		// this.sentenceRepository.delete(oldSen);
		// }

		// for (ArticleWordAsso awa : article.getWords()) {
		// this.articleWordAssoRepository.delete(awa);
		// }

		// WebDriver driver = new HtmlUnitDriver();
		// String wordVal = "dog";
		// driver.get("http://dict.cn/" + wordVal);
		// String pageSource = driver.getPageSource();
		// // System.out.println(pageSource);
		// String pronStr = "";
		// if (pageSource.contains("æ‚¨è¦æŸ¥æ‰¾çš„æ˜¯ä¸æ˜¯")) {
		// pronStr = "UNKNOWN";
		// }
		// else {
		// WebElement div = null;
		// try {
		// List<WebElement> pronList = driver.findElements(By
		// .xpath("//bdo[@lang='EN-US']"));
		// for (int i = 0; i < pronList.size(); i++) {
		// pronStr += pronList.get(i).getText() + " ";
		// }
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// pronStr = "UNKNOWN";
		// }
		// }
		// pronStr = StringUtils.trim(pronStr);
		// System.out.println("pronStr:" + pronStr);
	}

	@Test
	public void parseFile2() throws Exception {
		String arg = "test.txt";
		// option #1: By sentence.
		DocumentPreprocessor dp = new DocumentPreprocessor(arg);
		for (List sentence : dp) {
			System.out.println("###sentence:" + sentence);
		}
		// option #2: By token
		PTBTokenizer ptbt = new PTBTokenizer(new FileReader(arg),
				new CoreLabelTokenFactory(), "");
		for (CoreLabel label; ptbt.hasNext();) {
			label = (CoreLabel) ptbt.next();
			System.out.println("###label:" + label);
		}
	}

	@Test
	public void createArticle0() throws Exception {
		Article article = new Article();
		article.setId(-3L); // useless
		article.setName("-3.pdf");
		// article.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new
		// Date()));
		// article.setRemark("my comments ..........end");
		// article.setOpenFlag("Yes");
		// article.setType("doc");
		this.articleRepository.save(article);
	}

	@Test
	public void createArticle() throws Exception {
		// for (int i = 0; i < 100; i++) {
		// Article article = new Article();
		// article.setName("mock_" + i + ".txt");
		// article.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		// .format(new Date()));
		// article.setRemark("my comments " + i + "..........end");
		// article.setOpenFlag("Yes");
		// this.articleRepository.save(article);
		// }

		Article article = new Article();
		article.setName("Camel in Action.pdf");
		article.setLastUpt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		article.setRemark("my comments ..........end");
		article.setOpenFlag("Yes");
		article.setType("doc");
		this.articleRepository.save(article);

		long articleId = article.getId();
		long userId = 1;
		System.out.println("###articleId:" + articleId);

		this.articleService.removeOldData(articleId);
		this.articleService.parseArticle(articleId, userId, "");
	}

	@Test
	public void updateArticle() throws Exception {
		Article article = this.articleRepository.findOne(1L);
		article.setRemark("your comments..........end");
		this.articleRepository.save(article);
	}

	@Test
	public void removeOldData() throws Exception {
		this.articleService.removeOldData(4L);
		// this.articleService.test();
	}

	@Test
	@Transactional
	public void testJson() throws Exception {
		Article artile = this.articleRepository.findOne(1L);
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(artile);
		System.out.println("jsonString:" + jsonString);
		// this.articleService.test();
	}

	@Test
	public void parseArticle() throws Exception {
		// this.articleService.removeOldData(6L);
		long articleId = 2L;
		this.articleService.removeOldData(articleId);
		long userId = 1;
		this.articleService.parseArticle(articleId, userId, "");
	}

	@Test
	public void createUser() throws Exception {
		User user = new User();
		user.setName("Flag2");
		user.setEmail("zgq456@qq.com");
		user.setWechat("18958031393");
		user.setDegree("DO");
		user.setAge(30);
		user.setAboutMe("Be Yourself");
		user.setPassword("1234");
		// user.setName("ZXH");
		// user.setEmail("479851019@qq.com");
		// user.setWechat("479851019");
		// user.setDegree("BA");
		// user.setAge(31);
		// user.setAboutMe("own sucess");
		this.userRepository.save(user);
	}

	@Test
	public void createQuiz() throws Exception {
		Quiz quiz = new Quiz();
		quiz.setType(MyConstants.CUSTOM_QUIZ);
		User author = this.userRepository.findOne(1L);
		quiz.setAuthor(author);
		String lastUpt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		quiz.setLastUpt(lastUpt);
		quiz.setPassword("");
		quiz.setRemark("FCM related");
		this.quizRepository.save(quiz);
	}

	@Test
	public void testSen() throws Exception {
		List<SenSummary> senList = this.sentenceRepository.findByArticleUserId(1L);
		System.out.println("senList:" + senList.size());
	}

	@Test
	public void testWord() throws Exception {
		PageRequest pr = new PageRequest(0, 10);
		long userId = 1L;
		List<WordSummary> wordList = this.wordRepository.findByUserId(userId, pr)
				.getContent();
		System.out.println("wordList:" + wordList.size());
		for (int i = 0; i < wordList.size(); i++) {
			WordSummary ws = wordList.get(i);
			System.out.println("ws:" + ws + " hit:" + ws.getHit());
		}
	}

	@Test
	public void testArticleList() throws Exception {
		PageRequest pr = new PageRequest(0, 10);
		long userId = 1L;
		List<Article> articleList = this.articleRepository.findByUserId(userId, pr)
				.getContent();
		System.out.println("articleList:" + articleList.size());
	}

	@Test
	public void testWord2() throws Exception {
		long userId = 1L;
		List<Word> wordList = this.wordRepository.findByUserId3(userId, 0, 10,
				"explain2 desc"); // ,"value",
		// "value2",
		// "desc"
		;
		System.out.println("wordList:" + wordList.size());
		for (int i = 0; i < wordList.size(); i++) {
			Word word = wordList.get(i);
			System.out.println("word:" + word + " value:" + word.getValue() + " id:"
					+ word.getId() + " pron:" + word.getPron() + " explain:"
					+ word.getExplain2());
		}
	}

	@Test
	public void testWord4() throws Exception {
		long userId = 16L;
		long articleId = 233L;
		List<Word> wordList = this.wordRepository.findByUserId4(userId, articleId, 0, 10); // ,"value",
		// "value2",
		// "desc"
		;
		System.out.println("wordList:" + wordList.size());
		for (int i = 0; i < wordList.size(); i++) {
			Word word = wordList.get(i);
			System.out.println("word:" + word + " value:" + word.getValue() + " id:"
					+ word.getId() + " pron:" + word.getPron() + " explain:"
					+ word.getExplain2() + " Interest():" + word.getTempInterest());
		}
	}

	@Test
	public void testWordCount() throws Exception {
		long userId = 1L;
		long count = this.wordRepository.getWordTotalCount(userId);
		System.out.println("count:" + count);
	}

	@Test
	public void testSen2() throws Exception {
		long userId = 1L;
		List<Sentence> senList = this.sentenceRepository.findByArticleUserIdSubGrid(
				userId, "% able %");
		System.out.println("senList:" + senList.size());
		for (int i = 0; i < senList.size(); i++) {
			Sentence sen = senList.get(i);
			System.out.println("content" + i + ":" + sen.getContent() + " usefulFlag:"
					+ sen.getTempFlag());
		}
	}

	@Test
	public void testSen3() throws Exception {
		List<Sentence> senList = this.sentenceRepository.findInDummyArticle("% get %");
		System.out.println("senList:" + senList.size());
		for (int i = 0; i < senList.size(); i++) {
			Sentence sen = senList.get(i);
			System.out.println("content" + i + ":" + sen.getContent() + " usefulFlag:"
					+ sen.getTempFlag());
		}
	}

	@Test
	public void testGetWordListForQuiz() {
		long userId = 1L;
		List<QuizWordBean> quizWordBeanList = this.articleService
				.getWordListForQuiz(userId);

		System.out.println("###quizWordBeanList size:" + quizWordBeanList.size());
		System.out.println("###quizWordBeanList:" + quizWordBeanList);
	}

	@Test
	public void testGetWordListForQuiz2() {
		long quizId = 6L;
		List<QuizContent> quizContent = this.quizContentRepository.findByQuizId(quizId);
		System.out.println("###quizContent size:" + quizContent.size());
	}

	@Test
	public void testGetQuizRating() {
		long quizId = 7L;
		List<QuizRating> quizRating = this.articleService.getRateListForQuiz(quizId);
		System.out.println("###quizRating size:" + quizRating.size());
	}

	@Test
	public void testGetWordListFromQuizResult() {
		long userId = 1L;
		Set<Long> wordIdSet = new HashSet<Long>();
		AtomicInteger startQuesIndex = new AtomicInteger();
		List<QuizWordBean> qwBeanList = this.articleService.getWordListFromQuizResult(
				userId, wordIdSet, startQuesIndex);
		System.out.println("###qwBeanList size:" + qwBeanList.size());
		System.out.println("wordIdSet:" + wordIdSet);
		System.out.println("startQuesIndex:" + startQuesIndex.get());
	}

	@Test
	public void testAtomic() {
		AtomicInteger startQuesIndex = new AtomicInteger();
		System.out.println(startQuesIndex.incrementAndGet());
		System.out.println(startQuesIndex.incrementAndGet());
	}

	@Test
	public void testURLEncoder() {
		String url = "http://wlj.sturgeon.mopaas.com/zgq.jsp";
		try {
			url = URLEncoder.encode(url, "UTF-8");
			System.out.println("###url:" + url);
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
	}

	@Test
	public void testGetRateForQuiz() {
		long quizId = 1;
		long userId = 1;
		double rate = this.articleService.getRateForQuiz(quizId, userId);
		System.out.println("rate:" + rate);
	}

	@Test
	public void testGetSenList2() {
		long articleId = 226;
		long userId = 1;
		JqGridData<SenBean> senData = this.articleService.getSenList2(articleId, userId,
				10, 1);
		System.out.println("senData:" + senData.getJsonString());
	}

	@Test
	public void testJSON() {
		WebDriver driver = new HtmlUnitDriver();
		String url = "https://api.weixin.qq.com/sns/userinfo?access_token=OezXcEiiBSKSxW0eoylIePtkkyI7zFenA_gU_RL0J_c_RcvcEC1pJlFfWYqNj1juDHZUfYOze1gaULd_D1GCyEwLd0HfZVg2LXlmZ5I4t9IdJy_Q3Z5J9Vu1ZtvByjniaLNZ9C15CksUENEW858vvw&openid=oz1rJs-TVjVxCJVjMKgdqO_pFtR8&lang=zh_CN";
		driver.get(url);
		String pageSource = driver.getPageSource();
		try {
			pageSource = new String(pageSource.getBytes("ISO-8859-1"), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}
		System.out.println("pageSource:" + pageSource);

		// JsonElement jsonElm = new JsonParser().parse(pageSource);
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(pageSource);
			System.out.println("openid:" + json.get("openid"));
			System.out.println("nickname:" + json.get("nickname"));
		}
		catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new UnsupportedOperationException("Auto-generated method stub", e);
		}

	}

	/**
	 *
	 */
	@Test
	public void testTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
		System.out.println(new Date());
		System.out
				.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	}

	@Test
	public void testHttpClient() throws Exception {
		HttpClient httpClient = new HttpClient();
		// URI theUri = new URI(
		// "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=5JJzFJNi3mlBuIx0gRCxr8323nQjcnIreUfY87wl8rEOaItrvuInJDX8ile4RL9JCGW0Cvay0E2iWZOhE-D1zV_PcGvPK2Jom6m2uas252E",
		// false);
		String JSON_STRING = FileUtils.readFileToString(new File("test.json"));
		System.out.println(JSON_STRING);
		StringRequestEntity requestEntity = new StringRequestEntity(JSON_STRING,
				"application/json", "UTF-8");

		PostMethod postMethod = new PostMethod(
				"https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=1_5JJzFJNi3mlBuIx0gRCxr8323nQjcnIreUfY87wl8rEOaItrvuInJDX8ile4RL9JCGW0Cvay0E2iWZOhE-D1zV_PcGvPK2Jom6m2uas252E");
		postMethod.setRequestEntity(requestEntity);

		// HttpResponse response = httpClient.execut

		int statusCode = httpClient.executeMethod(postMethod);
		System.out.println("statusCode:" + statusCode);

		// {"errcode":0,"errmsg":"ok","msgid":205426623}

	}

	@Test
	public void testHttpClient2() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File jsonTemplateFile = new File(classLoader.getResource("test.json").getFile());
		System.out.println("jsonTemplateFile:" + jsonTemplateFile.getAbsolutePath());

		DefaultHttpClient httpClient = new DefaultHttpClient();
		System.out.println("jsonTemplateFile:" + jsonTemplateFile.getAbsolutePath());
		String JSON_STRING = FileUtils.readFileToString(jsonTemplateFile);
		System.out.println(JSON_STRING);
		StringEntity requestEntity = new StringEntity(JSON_STRING, "application/json",
				"UTF-8");
		String token = "a2R1gAnNzgTjrrVTjJhQ-WIoIjnRUnt1bBrLNUdrsW4Lj6B4NeUPtvAf60SAOZoBohD8AXzqujkHOozZKTz35J7v4BdMI3-RIKmhm5pTo1Vo";
		String uri = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="
				+ token;
		HttpPost method = new HttpPost(uri);
		method.setEntity(requestEntity);
		HttpResponse result = httpClient.execute(method);
		String resData = EntityUtils.toString(result.getEntity());
		JSONObject json = (JSONObject) new JSONParser().parse(resData);
		System.out.println("errcode:" + json.get("errcode"));
		System.out.println("errmsg:" + json.get("errmsg"));
		System.out.println("msgid:" + json.get("msgid"));

		// {"errcode":0,"errmsg":"ok","msgid":205426623}

	}
}
