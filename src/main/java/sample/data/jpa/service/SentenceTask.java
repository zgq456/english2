/*
 * Copyright 2012-2014 the original author or authors.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import sample.data.jpa.domain.Article;
import sample.data.jpa.domain.ArticleWordAsso;
import sample.data.jpa.domain.Sentence;
import sample.data.jpa.domain.Word;
import sample.data.jpa.repository.ArticleWordAssoRepository;
import sample.data.jpa.repository.SentenceRepository;
import sample.data.jpa.repository.WordRepository;

/**
 * @author Administrator
 */
public class SentenceTask implements Callable {
	List sentence = null;
	WordRepository wordRepository;
	SentenceRepository sentenceRepository;
	ArticleWordAssoRepository articleWordAssoRepository;
	Article article;
	Set<Sentence> sentences;

	public SentenceTask(List sentence, WordRepository wordRepository,
			SentenceRepository sentenceRepository,
			ArticleWordAssoRepository articleWordAssoRepository, Article article,
			Set<Sentence> sentences) {
		super();
		this.sentence = sentence;
		this.wordRepository = wordRepository;
		this.sentenceRepository = sentenceRepository;
		this.articleWordAssoRepository = articleWordAssoRepository;
		this.article = article;
		this.sentences = sentences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Object call() throws Exception {
		String sentenceStr = "";
		for (int i = 0; i < this.sentence.size(); i++) {
			Object oneWord = this.sentence.get(i);
			sentenceStr += (oneWord + " ");

			String lowCaseWord = oneWord.toString().toLowerCase();
			if ("...".equals(lowCaseWord) || ".".equals(lowCaseWord)
					|| ",".equals(lowCaseWord) || "■".equals(lowCaseWord)
					|| !lowCaseWord.matches("^[a-zA-Z\\-]*")) {
				System.out.println("ignore:" + lowCaseWord);
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
						.findByArticleIdAndWordId(this.article.getId(), dbWord.getId());
				if (awaDBList.isEmpty()) {
					awa = new ArticleWordAsso();
					awa.setArticle(this.article);
					awa.setWord(dbWord);
					awa.setHit(1);
				}
				else {
					awa = awaDBList.get(0);
					awa.setHit(awa.getHit() + 1);
				}
				this.articleWordAssoRepository.save(awa);
			}

		}
		Sentence oneSentence = new Sentence();
		oneSentence.setContent(sentenceStr);
		oneSentence.setArticle(this.article);
		this.sentenceRepository.save(oneSentence);
		this.sentences.add(oneSentence);

		return null;
	}

}
