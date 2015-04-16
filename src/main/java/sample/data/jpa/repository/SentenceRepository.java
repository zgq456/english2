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

import sample.data.jpa.domain.SenSummary;
import sample.data.jpa.domain.Sentence;

public interface SentenceRepository extends CrudRepository<Sentence, Long> {
	// @Query("select a from Sentence a left join UserSenAsso usa where a.article.user.id = :id and a.id = usa.sen.id and uas.user.id = :id")
	@Query("select new sample.data.jpa.domain.SenSummary(a, usa.usefulFlag) from Sentence a left join a.users usa where a.article.user.id = :id")
	Page<SenSummary> findByArticleUserId(@Param("id") Long userId, Pageable pageable);

	@Query(value = "select t1.id, t1.content, t1.create_date, t1.last_upt, t1.article_id, usa.useful_flag temp_flag from (select s.* from sentence s, article a  where s.article_id = a.id"
			+ " and a.user_id = :id and lower(s.content) like :wordStr ) as t1"
			+ " left join user_sen_asso usa on usa.sen_id = t1.id and usa.user_id = :id"
			+ " order by useful_flag desc, t1.last_upt desc limit 0, 50", nativeQuery = true)
	List<Sentence> findByArticleUserIdSubGrid(@Param("id") Long userId,
			@Param("wordStr") String word);

	// @Query("select a from Sentence a left join UserSenAsso usa where a.article.user.id = :id and a.id = usa.sen.id and uas.user.id = :id")
	// @Query("select a from Sentence a left join a.users usa where a.article.user.id = :id")
	@Query("select new sample.data.jpa.domain.SenSummary(a, usa.usefulFlag) from Sentence a left join a.users usa where a.article.user.id = :id")
	//
	// and a.id = usa.sen.id and uas.user.id = :id
	List<SenSummary> findByArticleUserId(@Param("id") Long userId);

	@Query("select count(a) from Sentence a where a.article.user.id = :id")
	public Long getUserTotalCount(@Param("id") Long id);

	@Query(value = "select id, content, create_date, last_upt, article_id, 1 temp_flag  from sentence where article_id = -1 and lower(content) like :wordStr", nativeQuery = true)
	List<Sentence> findInDummyArticle(@Param("wordStr") String word);
}
