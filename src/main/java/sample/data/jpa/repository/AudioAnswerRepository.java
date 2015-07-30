package sample.data.jpa.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import sample.data.jpa.domain.AudioAnswer;

public interface AudioAnswerRepository extends CrudRepository<AudioAnswer, Long> {
	// Page<Audio> findAll(Pageable pageable);
	List<AudioAnswer> findByAudioSnippetIdAndAuthorId(Long audioSnippetId, Long audioId);

	List<AudioAnswer> findByAudioSnippetIdOrderByRankDesc(Long audioSnippetId);
	// List<AudioSnippet> findByAudioId(Long audioId);
}
