package com.devillage.teamproject.controller.post;

import com.devillage.teamproject.dto.AuthDto;
import com.devillage.teamproject.dto.DoubleResponseDto;
import com.devillage.teamproject.dto.MultiResponseDto;
import com.devillage.teamproject.dto.PostDto;
import com.devillage.teamproject.entity.Bookmark;
import com.devillage.teamproject.entity.Post;
import com.devillage.teamproject.entity.ReportedPost;
import com.devillage.teamproject.security.util.JwtConstants;
import com.devillage.teamproject.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PostControllerImpl implements PostController {

    private final PostService postService;

    @Override
    public PostDto.Response postPost(@RequestHeader(JwtConstants.AUTHORIZATION_HEADER)String token, PostDto.Post request) {
        Post savedPost = postService.savePost(request.toEntity(), request.getCategory(), request.getTags(), token);
        return PostDto.Response.of(savedPost);
    }

    @Override
    public MultiResponseDto<PostDto.Response.PostDetail> getPost(Long id) {
        Post post = postService.getPost(id);
        return MultiResponseDto.of(PostDto.Response.PostDetail.of(post));
    }

    @Override
    public PostDto.Response patchPost(Long id, PostDto.Patch request) {
        Post post = request.toEntity();
        Post updatedPost = postService.editPost(id, post);
        return PostDto.Response.of(updatedPost);
    }

    @Override
    public PostDto.Response.BookmarkDto postBookmark(AuthDto.UserInfo userInfo, Long postId) {

        Bookmark bookmark = postService.postBookmark(userInfo.getId(), postId);
        return PostDto.Response.BookmarkDto.of(
                bookmark.getUser().getId(),
                bookmark.getPost().getId(),
                bookmark.getId());
    }

    @Override
    public PostDto.Response.ReportDto postReport(AuthDto.UserInfo userInfo, Long postId) {

        ReportedPost reportedPost = postService.postReport(userInfo.getId(), postId);
        return PostDto.Response.ReportDto.of(
                reportedPost.getUser().getId(),
                reportedPost.getPost().getId(),
                reportedPost.getId());
    }

    @Override
    public PostDto.Response.LikeDto postLike(AuthDto.UserInfo userInfo, Long postId) {

        Post post = postService.postLike(userInfo.getId(), postId);
        return PostDto.Response.LikeDto.of(
                post.getUser().getId(),
                post.getId(),
                post.getLikeCount());
    }

    @Override
    public DoubleResponseDto<PostDto.Response.SimplePostDto> getPostsByCategory(String category, int page, int size) {
        Page<Post> posts = postService.getPostsByCategory(category, page, size);
        return DoubleResponseDto.of(
                posts.stream()
                        .map(PostDto.Response.SimplePostDto::of)
                        .collect(Collectors.toList()),
                posts
        );
    }

    @Override
    public DoubleResponseDto<PostDto.Response.SimplePostDto> getPostsBySearch(String q, int page, int size) {
        Page<Post> posts = postService.getPostsBySearch(q, page, size);
        return DoubleResponseDto.of(
                posts.stream()
                        .map(PostDto.Response.SimplePostDto::of)
                        .collect(Collectors.toList()),
                posts
        );
    }

    @Override
    public DoubleResponseDto<PostDto.Response.SimplePostDto> getPostsByBookmark(AuthDto.UserInfo userInfo, int page, int size) {
        Page<Post> posts = postService.getPostsByBookmark(userInfo.getId(), page, size);
        return DoubleResponseDto.of(
                posts.stream()
                        .map(PostDto.Response.SimplePostDto::of)
                        .collect(Collectors.toList()),
                posts
        );
    }

    @Override
    public void deletePost(Long id) {

    }
}
