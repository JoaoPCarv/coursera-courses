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
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class VideoController {

	@Autowired
	private VideoFileManager videoFileManager;

	private static final String URL_PATTERN = "http://localhost:8080/video/%s/data";
	private final AtomicLong i = new AtomicLong(1L);
	private HashMap<Long, Video> videos = new HashMap<>();

	@RequestMapping(value = "/videos", method = RequestMethod.GET)
	public Map<Long, Video> getVideos() {
		return videos;
	}

	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public Video uploadVideo(@RequestBody Video video) {
		video.setId(i.getAndIncrement());
		video.setDataUrl(String.format(URL_PATTERN, video.getId()));
		videos.put(video.getId(), video);
		return video;
	}

	@RequestMapping(value = "/{id}/data", method = RequestMethod.POST)
	public VideoStatus uploadVideoData(
			@PathVariable("id") long id,
			@RequestParam("data") MultipartFile videoData,
			HttpServletResponse response) throws IOException {
		Video video = videos.get(id);
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("The requested video with id=%s was not found", id));
			return new VideoStatus(null);
		}
		videoFileManager.saveVideoData(video, videoData.getInputStream());
		return new VideoStatus(VideoStatus.VideoState.READY);
	}

	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
	public void getVideoData(@PathVariable("id") long id, HttpServletResponse response) throws IOException {

		Video video = videos.get(id);
		if (video == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("Video with id=%s not found", id));
			return;
		}
		if (!videoFileManager.hasVideoData(video)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("Video data for the id=%s not found", id));
			return;
		}

		response.setContentType("video/mpeg");
		videoFileManager.copyVideoData(video, response.getOutputStream());
	}
}