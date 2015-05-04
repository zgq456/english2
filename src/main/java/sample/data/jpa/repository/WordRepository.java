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

package sample.data.jpa.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import sample.data.jpa.domain.Word;
import sample.data.jpa.domain.WordSummary;

public interface WordRepository extends CrudRepository<Word, Long> {

	// Page<Word> findAll(Pageable pageable);
	//
	// Page<Word> findByNameContainingAndCountryContainingAllIgnoringCase(String name,
	// String country, Pageable pageable);
	//
	// Word findByNameAndCountryAllIgnoringCase(String name, String country);

	List<Word> findByLowValue(String lowValue);

	// @Query("select new sample.data.jpa.domain.WordSummary(w, uwa.rank, uwa.id) from Word w left join w.users uwa where uwa.user.id = :id")
	// @Query("select new sample.data.jpa.domain.WordSummary(w, 1, uaa.id) from Word w left join w.articles uaa left join w.users uwa where uaa.article.user.id = :id")
	@Query("select new sample.data.jpa.domain.WordSummary(w, 0, sum(uaa.hit)) from Word w left join w.articles uaa where uaa.article.user.id = :id and uaa.article.deleteFlag = 0 and uaa.article.hideFlag = 'No' group by w.id order by uaa.hit desc ")
	// and uwa.user.id = :id
	// left join w.users uwa
	// group by w.id order by uaa.hit desc
	Page<WordSummary> findByUserId(@Param("id") Long userId, Pageable pageable);

	// @Query(value =
	// "select t1.word_id id, t1.value content, t1.explain2, t1.pron, t1.hit, uwo.rank, uwo.user_id from ("
	// +
	// " select w.id word_id, w.value, w.explain2, w.pron, sum(awa.hit) hit from word w,  article a , article_word_asso awa "
	// + " where w.id = awa.word_id and a.id = awa.article_id and a.user_id = :id "
	// + "	group by w.id "
	// + " ) as t1 left join user_word_asso uwo on t1.word_id = uwo.word_id", name =
	// "testResult", nativeQuery = true)
	// List<WordBean> findByUserId2(@Param("id") Long userId); // , Pageable pageable

	@Query(value = "select t1.word_id id, t1.word_id id_sort, t1.value, t1.value value_sort, t1.create_date, t1.explain2, "
			+ " t1.explain2 explain_sort, t1.level, t1.pron, t1.pron pron_sort, t1.last_upt, t1.low_value, t1.mark, t1.hit temp_hit, t1.hit hit_sort, "
			+ " uwo.rank temp_rank, uwo.rank rank_sort, t1.user_id temp_user_id from ("
			+ " select w.id word_id, w.value, w.explain2, w.pron, sum(awa.hit) hit, w.low_value, w.create_date, "
			+ " w.last_upt, w.mark, a.user_id, w.level from word w,  article a , article_word_asso awa "
			+ " where w.id = awa.word_id and a.id = awa.article_id and a.user_id = :id and a.delete_flag = 0 and a.hide_flag = 'No' "
			+ "	group by w.id "
			+ " ) as t1 left join user_word_asso uwo on t1.word_id = uwo.word_id and uwo.user_id = :id order by "
			+ "case when :sort = 'id asc' then id_sort end asc, "
			+ "case when :sort = 'id desc' then id_sort end desc, "
			+ "case when :sort = 'pron asc' then pron_sort end asc, "
			+ "case when :sort = 'pron desc' then pron_sort end desc, "
			+ "case when :sort = 'hit asc' then hit_sort end asc, "
			+ "case when :sort = 'hit desc' then hit_sort end desc, "
			+ "case when :sort = 'rank asc' then rank_sort end asc, "
			+ "case when :sort = 'rank desc' then rank_sort end desc, "
			+ "case when :sort = 'explain2 asc' then explain_sort end asc, "
			+ "case when :sort = 'explain2 desc' then explain_sort end desc, "
			+ "case when :sort = 'level asc' then level end asc, "
			+ "case when :sort = 'level desc' then level end desc, "
			+ "case when :sort = 'value asc' then value_sort end asc, "
			+ "case when :sort = 'value desc' then value_sort end desc "
			+ ", hit desc limit :page, :size", nativeQuery = true)
	List<Word> findByUserId3(@Param("id") Long userId, @Param("page") int page,
			@Param("size") int size, @Param("sort") String sort); // ,, @Param("sidx")

	@Query(value = "select t1.word_id id, t1.word_id id_sort, t1.value, t1.value value_sort, t1.create_date, t1.explain2, "
			+ " t1.explain2 explain_sort, t1.level, t1.pron, t1.pron pron_sort, t1.last_upt, t1.low_value, t1.mark, t1.hit temp_hit, t1.hit hit_sort, "
			+ " uwo.rank temp_rank, uwo.rank rank_sort, t1.user_id temp_user_id, uwo.interest temp_interest from ("
			+ " select w.id word_id, w.value, w.explain2, w.pron, sum(awa.hit) hit, w.low_value, w.create_date, "
			+ " w.last_upt, w.mark, a.user_id, w.level from word w,  article a , article_word_asso awa "
			+ " where w.id = awa.word_id and a.id = awa.article_id and a.user_id = :id and a.id = :articleId and a.delete_flag = 0 and a.hide_flag = 'No' "
			+ "	group by w.id "
			+ " ) as t1 left join user_word_asso uwo on t1.word_id = uwo.word_id and uwo.user_id = :id order by "
			+ " level asc, hit desc limit :page, :size", nativeQuery = true)
	List<Word> findByUserId4(@Param("id") Long userId,
			@Param("articleId") Long articleId, @Param("page") int page,
			@Param("size") int size); // ,, @Param("sidx")
										// String sidx,

	// @Query(value = "select count(*)  from ("
	// +
	// " select w.id word_id, w.value, w.explain2, w.pron, sum(awa.hit) hit, w.low_value, w.create_date, w.last_upt, "
	// + " w.mark, a.user_id from word w,  article a , article_word_asso awa "
	// +
	// " where w.id = awa.word_id and a.id = awa.article_id and a.user_id = :id and a.delete_flag = 0 and a.hide_flag = 'No' "
	// + "	group by w.id "
	// + " ) as t1 left join user_word_asso uwo on t1.word_id = uwo.word_id", name =
	// "testResult", nativeQuery = true)

	@Query(value = "select count(*) from ("
			+ " select w.id word_id, w.value, w.explain2, w.pron, sum(awa.hit) hit, w.low_value, w.create_date, "
			+ " w.last_upt, w.mark, a.user_id, w.level from word w,  article a , article_word_asso awa "
			+ " where w.id = awa.word_id and a.id = awa.article_id and a.user_id = :id and a.delete_flag = 0 and a.hide_flag = 'No' "
			+ "	group by w.id "
			+ " ) as t1 left join user_word_asso uwo on t1.word_id = uwo.word_id and uwo.user_id = :id   ", nativeQuery = true)
	Long getWordTotalCount(@Param("id") Long userId);

	@Query(value = "select count(*) from ("
			+ " select w.id word_id, w.value, w.explain2, w.pron, sum(awa.hit) hit, w.low_value, w.create_date, "
			+ " w.last_upt, w.mark, a.user_id, w.level from word w,  article a , article_word_asso awa "
			+ " where w.id = awa.word_id and a.id = awa.article_id and a.user_id = :id and a.id = :articleId and a.delete_flag = 0 and a.hide_flag = 'No' "
			+ "	group by w.id "
			+ " ) as t1 left join user_word_asso uwo on t1.word_id = uwo.word_id and uwo.user_id = :id   ", nativeQuery = true)
	Long getWordTotalCount2(@Param("id") Long userId, @Param("articleId") Long articleId);
}
