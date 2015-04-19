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

import org.springframework.data.repository.CrudRepository;

import sample.data.jpa.domain.QuizResult;

public interface QuizResultRepository extends CrudRepository<QuizResult, Long> {
	public void deleteByUserIdAndQuizId(Long userId, Long quizId);

	public List<QuizResult> findByUserIdAndWordId(Long userId, Long wordId);

	// @Query("select new Article(a.id, a.name, a.url, a.remark, a.lastUpt, a.createDate, a.openFlag, a.type, a.deleteFlag, a.hideFlag, a.user) from Article a where a.user.id = :id and a.deleteFlag = 0")
	// Page<Article> findByUserId(@Param("id") long userId, Pageable pageable);

}
