package com.testwa.distest.server.web.issue.mgr;

import com.testwa.distest.server.entity.Issue;
import com.testwa.distest.server.entity.IssueComment;
import com.testwa.distest.server.entity.IssueLabel;
import com.testwa.distest.server.entity.User;
import com.testwa.distest.server.service.issue.dto.IssueStateCountDTO;
import com.testwa.distest.server.service.issue.form.IssueListForm;
import com.testwa.distest.server.service.issue.service.IssueService;
import com.testwa.distest.server.service.issue.service.LabelService;
import com.testwa.distest.server.service.user.service.UserService;
import com.testwa.distest.server.web.auth.vo.UserVO;
import com.testwa.distest.server.web.issue.vo.IssueCommentVO;
import com.testwa.distest.server.web.issue.vo.IssueLabelVO;
import com.testwa.distest.server.web.issue.vo.IssueStateCountVO;
import com.testwa.distest.server.web.issue.vo.IssueVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author wen
 * @create 2018-12-26 11:01
 */
@Component
@Slf4j
public class IssueMgr {

    @Autowired
    private IssueService issueService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private UserService userService;


    public List<IssueVO> buildIssueVOList(List<Issue> issues) {
        return issues.stream().map(this::buildIssueVO).collect(Collectors.toList());
    }

    public IssueVO buildIssueVO(Issue issue) {
        IssueVO vo = new IssueVO();
        BeanUtils.copyProperties(issue, vo);

        // 获得 issue 的标签列表
        List<IssueLabel> issueLabels = labelService.listByIssueId(issue.getId());
        List<IssueLabelVO> lableVOs = issueLabels.stream().map(lable -> {
            IssueLabelVO labelVO = new IssueLabelVO();
            BeanUtils.copyProperties(lable, labelVO);
            return labelVO;
        }).collect(Collectors.toList());
        vo.setLabels(lableVOs);

        // 获得创建人
        User author = userService.get(issue.getAuthorId());
        UserVO authorVO = new UserVO();
        BeanUtils.copyProperties(author, authorVO);
        vo.setAuthor(authorVO);
        // 获得指派者
        if(issue.getAssigneeId() != null) {
            User assignee = userService.get(issue.getAssigneeId());
            UserVO assigneeVO = new UserVO();
            BeanUtils.copyProperties(assignee, assigneeVO);
            vo.setAssignee(assigneeVO);
        }

        return vo;
    }

    public IssueStateCountVO getStateCountVO(IssueListForm form, Long projectId) {
        IssueStateCountVO vo = new IssueStateCountVO();
        vo.setRejected(0L);
        vo.setClosed(0L);
        vo.setOpen(0L);
        List<IssueStateCountDTO> countDTOList = issueService.getCountGroupByState(form, projectId);
        countDTOList.forEach( dto -> {
            switch(dto.getIssueState()) {
                case OPEN:
                    vo.setOpen(dto.getCountValue());
                    break;
                case CLOSED:
                    vo.setClosed(dto.getCountValue());
                    break;
                case REJECT:
                    vo.setRejected(dto.getCountValue());
                    break;
            }
        });

        return vo;
    }

    public List<IssueCommentVO> buildIssueCommentVOs(List<IssueComment> commentList) {

        if(!commentList.isEmpty()) {
            return commentList.stream().map(comment -> {
                IssueCommentVO vo = new IssueCommentVO();
                BeanUtils.copyProperties(comment, vo);
                User assignee = userService.get(comment.getAuthorId());
                UserVO assigneeVO = new UserVO();
                BeanUtils.copyProperties(assignee, assigneeVO);
                vo.setAuthor(assigneeVO);
                return vo;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
