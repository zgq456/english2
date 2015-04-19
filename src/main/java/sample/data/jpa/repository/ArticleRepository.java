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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import sample.data.jpa.domain.Article;

public interface ArticleRepository extends CrudRepository<Article, Long> {
	Page<Article> findAll(Pageable pageable);

	// @Column(nullable = true)
	// private String url;
	//
	// @Column(nullable = true)
	// private String remark;
	//
	// @Column(nullable = true)
	// private String lastUpt;
	//
	// @Column(nullable = true)
	// private String createDate;
	//
	// @Column(nullable = true)
	// private String openFlag;
	//
	// @Column(nullable = true)
	// private String type;

	// Long id, String name, String url, String remark, String lastUpt,
	// String createDate, String openFlag, String type, int deleteFlag

	// @Query("select a.id, a.name, a.url, a.remark, a.lastUpt, a.createDate, a.openFlag, a.type from Article a where a.user.id = :id and a.deleteFlag = 0")
	@Query("select new Article(a.id, a.name, a.url, a.remark, a.lastUpt, a.createDate, a.openFlag, a.type, a.deleteFlag, a.hideFlag, a.user) from Article a where a.user.id = :id and a.deleteFlag = 0")
	Page<Article> findByUserId(@Param("id") long userId, Pageable pageable);

	@Query("select count(a) from Article a where a.user.id = :id  and a.deleteFlag = 0")
	public Long getUserTotalCount(@Param("id") Long id);
}
