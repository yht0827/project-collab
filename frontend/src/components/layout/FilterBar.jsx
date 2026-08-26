import React from 'react';
import { UI_TEXT } from '../../constants/uiText';

export default function FilterBar({
  keywordInput,
  onKeywordChange,
  onClearKeyword,
  totalElements,
  statusFilter,
  onStatusFilterChange,
  projectLabels = [],
  labelFilter,
  onLabelFilterChange,
  pageSize,
  onPageSizeChange
}) {
  return (
    <div className="filter-bar">
      <div className="search-box">
        <input
          type="text"
          className="search-input"
          placeholder={UI_TEXT.SEARCH_PLACEHOLDER}
          value={keywordInput}
          onChange={(e) => onKeywordChange(e.target.value)}
        />
        {keywordInput && (
          <button
            className="search-clear-btn"
            onClick={onClearKeyword}
            title={UI_TEXT.TOOLTIP_CLEAR_SEARCH}
          >
            ✕
          </button>
        )}
      </div>

      <div className="filter-group">
        <span className="search-count-badge">
          {UI_TEXT.TOTAL_COUNT_PREFIX} {totalElements}{UI_TEXT.TOTAL_COUNT_SUFFIX}
        </span>

        {/* 라벨 필터 */}
        <select
          className="filter-select"
          value={labelFilter || ''}
          onChange={(e) => onLabelFilterChange(e.target.value ? Number(e.target.value) : '')}
        >
          <option value="">{UI_TEXT.LABEL_ALL}</option>
          {projectLabels.map(l => (
            <option key={l.id} value={l.id}>{l.name}</option>
          ))}
        </select>

        {/* 상태 필터 */}
        <select
          className="filter-select"
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value)}
        >
          <option value="">{UI_TEXT.STATUS_ALL}</option>
          <option value="TODO">{UI_TEXT.STATUS_TODO}</option>
          <option value="IN_PROGRESS">{UI_TEXT.STATUS_IN_PROGRESS}</option>
          <option value="DONE">{UI_TEXT.STATUS_DONE}</option>
        </select>

        {/* 페이지 크기 */}
        <select
          className="filter-select"
          value={pageSize}
          onChange={(e) => onPageSizeChange(Number(e.target.value))}
        >
          <option value={10}>{UI_TEXT.PER_PAGE_10}</option>
          <option value={20}>{UI_TEXT.PER_PAGE_20}</option>
          <option value={50}>{UI_TEXT.PER_PAGE_50}</option>
        </select>
      </div>
    </div>
  );
}
