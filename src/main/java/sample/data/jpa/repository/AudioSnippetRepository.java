package sample.data.jpa.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import sample.data.jpa.domain.AudioSnippet;

public interface AudioSnippetRepository extends CrudRepository<AudioSnippet, Long> {
	// Page<Audio> findAll(Pageable pageable);
	Page<AudioSnippet> findByAudioIdOrderByStartAsc(Pageable pageable, Long audioId);

	List<AudioSnippet> findByAudioIdAndStart(Long audioId, int start);
}
