package com.devillage.teamproject.service.post;

import com.devillage.teamproject.entity.*;
import com.devillage.teamproject.entity.enums.CategoryType;
import com.devillage.teamproject.entity.enums.ReportType;
import com.devillage.teamproject.exception.BusinessLogicException;
import com.devillage.teamproject.exception.ExceptionCode;
import com.devillage.teamproject.repository.category.CategoryRepository;
import com.devillage.teamproject.repository.post.BookmarkRepository;
import com.devillage.teamproject.repository.post.LikeRepository;
import com.devillage.teamproject.repository.post.PostRepository;
import com.devillage.teamproject.repository.post.ReportedPostRepository;
import com.devillage.teamproject.repository.posttag.PostTagRepository;
import com.devillage.teamproject.repository.tag.TagRepository;
import com.devillage.teamproject.repository.user.UserRepository;
import com.devillage.teamproject.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReportedPostRepository reportedPostRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;
    private final UserRepository userRepository;

    @Override
    public Post savePost(Post post, CategoryType categoryType, List<String> tagValue, Long userId) {
        User findUser = userRepository.findById(userId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        Category category = categoryRepository.findCategoriesByCategoryType(categoryType);

        if(tagValue.size()==0 || tagValue.isEmpty()){
            PostTag postTag = new PostTag();
            postTagRepository.save(postTag);
            post.addPostTag(postTag);
            post.addCategory(category);
            postRepository.save(post);
            findUser.addPost(post);
            post.addUser(findUser);
        }
        tagValue.forEach(
                e -> {
                    if (tagRepository.findTagByName(e).isEmpty()) {
                        Tag tag = tagRepository.save(new Tag(e));
                        PostTag postTag = new PostTag(post, tag);
                        postTagRepository.save(postTag);
                        post.addPostTag(postTag);
                        post.addCategory(category);
                        postRepository.save(post);
                        findUser.addPost(post);
                        post.addUser(findUser);
                    } else {
                        Tag tag = tagRepository.findTagByName(e).orElseThrow(IllegalArgumentException::new);
                        PostTag postTag = new PostTag(post, tag);
                        postTagRepository.save(postTag);
                        post.addPostTag(postTag);
                        post.addCategory(category);
                        postRepository.save(post);
                        findUser.addPost(post);
                        post.addUser(findUser);
                    }
                }
        );

        return post;
    }

    @Override
    public Post editPost(Post post, CategoryType categoryType, List<String> tagValue, Long userId, Long postId) {
        Post verifiedPost = findVerifyPost(postId);
        User findUser = userRepository.findById(userId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_NOT_FOUND));

        Category category = categoryRepository.findCategoriesByCategoryType(categoryType);
        postTagRepository.deleteByPostId(postId);

        if(tagValue.size()==0 || tagValue.isEmpty()){
            PostTag postTag = new PostTag();
            postTagRepository.save(postTag);
            verifiedPost.addPostTag(postTag);
            verifiedPost.addCategory(category);
            verifiedPost.editPost(post);
            postRepository.save(verifiedPost);
            findUser.addPost(verifiedPost);
            verifiedPost.addUser(findUser);
        }
        tagValue.forEach(
                e -> {
                    if (tagRepository.findTagByName(e).isEmpty()) {
                        Tag tag = tagRepository.save(new Tag(e));
                        PostTag postTag = new PostTag(verifiedPost, tag);
                        postTagRepository.save(postTag);
                        verifiedPost.addPostTag(postTag);
                        verifiedPost.addCategory(category);
                        verifiedPost.editPost(post);
                        postRepository.save(verifiedPost);
                        findUser.addPost(verifiedPost);
                        verifiedPost.addUser(findUser);
                    } else {
                        Tag tag = tagRepository.findTagByName(e).orElseThrow(IllegalArgumentException::new);
                        PostTag postTag = new PostTag(verifiedPost, tag);
                        postTagRepository.save(postTag);
                        verifiedPost.addPostTag(postTag);
                        verifiedPost.addCategory(category);
                        verifiedPost.editPost(post);
                        postRepository.save(verifiedPost);
                        findUser.addPost(verifiedPost);
                        verifiedPost.addUser(findUser);

                    }
                }
        );
        return verifiedPost;
    }

    @Override
    public void deletePost(Long postId) {
        findVerifyPost(postId);
        postRepository.deleteById(postId);
    }

    @Override
    public Post getPost(Long userId) {
        Post post = findVerifyPost(userId);
        post.setClickCount(updateClicks(post));
        return post;
    }

    @Override
    public Page<Post> getPostsByCategory(String category, int page, int size) {
        try {
            CategoryType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException(ExceptionCode.CATEGORY_NOT_FOUND);
        }

        if (category.toUpperCase().equals(CategoryType.ALL.name())) {
            return postRepository.findAll(
                    PageRequest.of(page - 1, size, Sort.by("id").descending()));
        }

        return postRepository.findDistinctByCategory_CategoryType(
                CategoryType.valueOf(category.toUpperCase()),
                PageRequest.of(page - 1, size, Sort.by("id").descending()));
    }

    @Override
    public Page<Post> getPostsBySearch(String word, int page, int size) {
        return postRepository.findDistinctByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                word, word, PageRequest.of(page - 1, size, Sort.by("id").descending()));
    }

    @Override
    public Page<Post> getPostsByTag(String tagName, int page, int size) {
        Tag tag = tagRepository.findTagByName(tagName)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.TAG_NOT_FOUND));
        Page<PostTag> postTags = postTagRepository.findDistinctByTag(tag,
                PageRequest.of(page - 1, size, Sort.by("id").descending()));
        List<Post> posts = postTags.stream()
                .map(PostTag::getPost)
                .collect(Collectors.toList());

        return new PageImpl<>(posts,
                PageRequest.of(page - 1, size),
                postTags.getTotalElements());
    }

    @Override
    public Page<Post> getPostsByBookmark(Long userId, int page, int size) {
        User user = userService.findVerifiedUser(userId);

        List<Post> postsList = user.getBookmarks()
                .stream()
                .sorted(Comparator.comparing(bookmark -> -bookmark.getId()))
                .map(Bookmark::getPost)
                .collect(Collectors.toList());

        int start = (page - 1) * size;
        int end = Math.min(start + size, postsList.size());

        return new PageImpl<>(postsList.subList(start, end),
                PageRequest.of(page - 1, size),
                postsList.size());
    }


    @Override
    public Bookmark postBookmark(Long userId, Long postId) {
        User user = userService.findVerifiedUser(userId);
        Post post = findVerifyPost(postId);

        List<Bookmark> findBookmark = bookmarkRepository.findByUserIdAndPostId(userId, postId);
        Bookmark bookmark;

        if (!findBookmark.isEmpty()) {
            bookmark = findBookmark.get(0);
            bookmarkRepository.deleteAll(findBookmark);
        } else {
            bookmark = new Bookmark(user, post);
            user.addBookmark(bookmark);
        }

        return bookmark;
    }

    @Override
    public ReportedPost postReport(Long userId, Long postId, Integer reportType, String content) {

        if (reportType == null || reportType < 1 || reportType > ReportType.values().length) {
            throw new BusinessLogicException(ExceptionCode.REPORT_TYPE_NOT_FOUND);
        }

        User user = userService.findVerifiedUser(userId);
        Post post = findVerifyPost(postId);

        reportedPostRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(reportedPost -> {
                    throw new BusinessLogicException(ExceptionCode.ALREADY_REPORTED);
                });

        ReportedPost reportedPost = new ReportedPost(user, post, ReportType.values()[reportType - 1], content);
        post.addReportedPosts(reportedPost);
        return reportedPost;
    }

    @Override
    public Post postLike(Long userId, Long postId) {
        User user = userService.findVerifiedUser(userId);
        Post post = findVerifyPost(postId);

        List<Like> findLikes = likeRepository.findByUserIdAndPostId(userId, postId);
        Long count = likeRepository.countByPostId(postId);

        if (!findLikes.isEmpty()) {
            likeRepository.deleteAll(findLikes);
            count -= 1L;
        } else {
            Like like = new Like(user, post);
            user.addLike(like);
            count += 1L;
        }

        post.setLikeCount(count);
        return post;
    }

    @Override
    public Post findVerifyPost(Long postId) {
        Optional<Post> findPost = postRepository.findById(postId);

        return findPost.orElseThrow(
                () -> new BusinessLogicException(ExceptionCode.POST_NOT_FOUND)
        );
    }
    public Long updateClicks(Post post){
        Long count = post.getClicks()+1L;
        return count;
    }

}
