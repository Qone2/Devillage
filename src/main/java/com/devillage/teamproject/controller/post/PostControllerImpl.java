package com.devillage.teamproject.controller.post;

import com.devillage.teamproject.dto.MultiResponseDto;
import com.devillage.teamproject.dto.PostDto;
import com.devillage.teamproject.dto.SingleResponseDto;
import com.devillage.teamproject.entity.Bookmark;
import com.devillage.teamproject.entity.Like;
import com.devillage.teamproject.entity.Post;
import com.devillage.teamproject.entity.ReportedPost;
import com.devillage.teamproject.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostControllerImpl implements PostController {

    private final PostService postService;

    @Override
    public SingleResponseDto postPost(@Valid @RequestBody PostDto.Post request) {
        Post post = PostDto.Post.toEntity(request);
        Post savedPost = postService.savePost(post);

        return SingleResponseDto.of((PostDto.Response.of(savedPost)));
    }

    @Override
    public MultiResponseDto<PostDto.Response.PostDetail> getPost(Long id) {
        Post post = postService.getPost(id);
        return MultiResponseDto.PostDetail.of(PostDto.Response.PostDetail.of(post));
    }

    @Override
    public SingleResponseDto<PostDto.Response.BookmarkDto> postBookmark(Long postId) {
        Long userId = 1L; // Security 메서드 구현 필요
        Bookmark bookmark = postService.postBookmark(postId);
        return SingleResponseDto.of(
                PostDto.Response.BookmarkDto.of(userId, postId, bookmark.getId())
        );
    }

    @Override
    public SingleResponseDto<PostDto.Response.ReportDto> postReport(Long postId) {
        Long userId = 1L; // Security 메서드 구현 필요
        ReportedPost reportedPost = postService.postReport(postId);
        return SingleResponseDto.of(
                PostDto.Response.ReportDto.of(userId, postId, reportedPost.getId())
        );
    }

    @Override
    public SingleResponseDto<PostDto.Response.LikeDto> postLike(Long postId) {
        Long userId = 1L; // Security 메서드 구현 필요
        Like like = postService.postLike(postId);
        return SingleResponseDto.of(
                PostDto.Response.LikeDto.of(userId, postId, like.getId())
        );
    }

    @Override
    public Long patchPost(Long id, PostDto.Patch request) {
        return null;
    }

    @Override
    public List<PostDto.Response> getPosts(String category, Long page, Long size) {
        return null;
    }

    @Override
    public void deletePost(Long id) {

    }
}
