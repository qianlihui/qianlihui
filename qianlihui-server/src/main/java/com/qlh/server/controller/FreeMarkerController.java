/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package com.qlh.server.controller;

import com.qlh.server.domain.entity.PlaylistEntry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Controller
public class FreeMarkerController {

    @RequestMapping("/freemarker")
    public String index() {
        return "freemarker/index";
    }


    @RequestMapping("/smalllist.freemarker")
    public String smallList(final Model model) {
        List<PlaylistEntry> list = getData();
        model.addAttribute("entries", list);
        return "freemarker/smalllist";
    }


    @RequestMapping("/biglist.freemarker")
    public String bigListFreeMarker(final Model model) {

        final List<PlaylistEntry> playlistEntries = getData();
        model.addAttribute("dataSource", playlistEntries);

        return "freemarker/biglist";

    }

    private List<PlaylistEntry> getData() {
       return Stream.of(1, 2, 3, 4, 5).map(e ->
                new PlaylistEntry()
                        .setPlaylistId(e)
                        .setPlaylistName("PlaylistName " + e)
                        .setTrackName("TrackName " + e)
                        .setAlbumTitle("AlbumTitle " + e)
                        .setArtistName("ArtistName " + e)
        ).collect(Collectors.toList());
    }
}
