/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;

@RestController
@RequestMapping("/video")
public class VideoController {

	private final VideoRepository videoRepository;

	public VideoController(VideoRepository videoRepository) {
		this.videoRepository = videoRepository;
	}

	private Video findVideoById(long id) {
		return videoRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("Video with id=%s not found", id)));
	}

	@GetMapping
	public Collection<Video> getVideos() {
		return videoRepository.findAll();
	}

	@PostMapping
	public Video addVideo(@RequestBody Video video) {
		video.setLikes(0);
		video.setLikedBy(new HashSet<>());
		return videoRepository.save(video);
	}

	@GetMapping("/{id}")
	public Video getById(@PathVariable long id) {
		return findVideoById(id);
	}

	@PostMapping("/{id}/like")
	@ResponseStatus(HttpStatus.OK)
	public void likeVideo(@PathVariable long id, Principal principal) {
		var video = findVideoById(id);
		var username = principal.getName();
		if (video.getLikedBy().contains(username)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"User in session has already liked this video");
		}
		video.getLikedBy().add(username);
		video.setLikes(video.getLikes() + 1);
		videoRepository.save(video);
	}

	@PostMapping("/{id}/unlike")
	public void unlikeVideo(@PathVariable long id, Principal principal, HttpServletResponse response) {
		var video = findVideoById(id);
		var username = principal.getName();
		if (!video.getLikedBy().contains(username)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"User in session has not liked this video");
		}
		video.getLikedBy().remove(username);
		video.setLikes(video.getLikes() - 1);
		videoRepository.save(video);
		response.setStatus(HttpStatus.OK.value());
	}

	@GetMapping("/search/findByName")
	public Collection<Video> findByName(@RequestParam String title) {
		return videoRepository.findByName(title);
	}

	@GetMapping("/search/findByDurationLessThan")
	public Collection<Video> findByDurationLessThan(@RequestParam long duration) {
		return videoRepository.findByDurationLessThan(duration);
	}
}