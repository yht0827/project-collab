package com.example.projectcollab.label.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projectcollab.common.exception.BusinessException;
import com.example.projectcollab.common.exception.ErrorCode;
import com.example.projectcollab.label.dto.LabelDto;
import com.example.projectcollab.label.entity.Label;
import com.example.projectcollab.label.repository.LabelRepository;
import com.example.projectcollab.label.repository.TaskLabelRepository;
import com.example.projectcollab.project.entity.Project;
import com.example.projectcollab.project.entity.ProjectMember;
import com.example.projectcollab.project.repository.ProjectRepository;
import com.example.projectcollab.project.service.ProjectMemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabelService {

	private final LabelRepository labelRepository;
	private final TaskLabelRepository taskLabelRepository;
	private final ProjectRepository projectRepository;
	private final ProjectMemberService projectMemberService;

	public List<LabelDto.Response> getProjectLabels(Long currentUserId, Long projectId) {
		projectMemberService.findMember(projectId, currentUserId);
		return labelRepository.findByProjectIdOrderByNameAsc(projectId)
			.stream()
			.map(LabelDto.Response::from)
			.toList();
	}

	@Transactional
	public LabelDto.Response createLabel(Long currentUserId, Long projectId, LabelDto.CreateRequest request) {
		projectMemberService.validateManager(projectId, currentUserId);

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

		if (labelRepository.existsByProjectIdAndName(projectId, request.name().trim())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		Label label = Label.createLabel(project, request.name().trim(), request.color());

		Label saved = labelRepository.save(label);
		log.info("[LABEL] 라벨 생성 완료 - projectId={}, labelId={}, name='{}'", projectId, saved.getId(), saved.getName());
		return LabelDto.Response.from(saved);
	}

	@Transactional
	public void deleteLabel(Long currentUserId, Long projectId, Long labelId) {
		projectMemberService.validateManager(projectId, currentUserId);

		Label label = labelRepository.findByIdAndProjectId(labelId, projectId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

		taskLabelRepository.deleteByLabelId(labelId);
		labelRepository.delete(label);
		log.info("[LABEL] 라벨 삭제 완료 - projectId={}, labelId={}", projectId, labelId);
	}
}
