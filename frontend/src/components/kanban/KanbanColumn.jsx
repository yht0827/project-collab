import React from 'react';
import { UI_TEXT } from '../../constants/uiText';

function getDueInfo(dueDateStr, isDone) {
  if (!dueDateStr) return null;
  if (isDone) {
    return { text: UI_TEXT.DUE_DONE(dueDateStr), className: 'bg-slate-100 text-slate-500 border-slate-200' };
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const due = new Date(dueDateStr);
  due.setHours(0, 0, 0, 0);

  const diffTime = due - today;
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays < 0) {
    return { text: UI_TEXT.DUE_OVERDUE(Math.abs(diffDays)), className: 'bg-red-50 text-red-600 border-red-200' };
  } else if (diffDays === 0) {
    return { text: UI_TEXT.DUE_TODAY, className: 'bg-orange-50 text-orange-600 border-orange-200' };
  } else if (diffDays <= 3) {
    return { text: UI_TEXT.DUE_NEAR(diffDays, dueDateStr), className: 'bg-amber-50 text-amber-700 border-amber-200' };
  } else {
    return { text: UI_TEXT.DUE_NORMAL(dueDateStr), className: 'bg-slate-100 text-slate-600 border-slate-200' };
  }
}

export default function KanbanColumn({ title, badgeClass, tasks, onStatusChange, onEditTask, onDeleteTask }) {
  return (
    <div className="bg-slate-100 p-3 rounded-2xl flex flex-col gap-2 h-full min-h-0 border border-slate-200">
      <div className="flex items-center justify-between px-1 flex-shrink-0">
        <h3 className="text-xs font-bold text-slate-700">{title}</h3>
        <span className={`text-[11px] font-bold px-2 py-0.5 rounded-xl ${badgeClass}`}>
          {tasks.length}
        </span>
      </div>

      <div className="flex flex-col gap-2 overflow-y-auto flex-1 pr-1">
        {tasks.length === 0 ? (
          <div className="text-center py-10 text-xs text-slate-400 border border-dashed border-slate-300 rounded-xl">
            {UI_TEXT.NO_TASKS}
          </div>
        ) : (
          tasks.map(t => {
            const dueInfo = getDueInfo(t.dueDate, t.status === 'DONE');

            return (
              <div
                key={t.id}
                className="bg-white p-3 rounded-xl border border-slate-200 shadow-sm hover:shadow transition flex flex-col gap-2 flex-shrink-0"
              >
                {/* 1. 라벨 태그 */}
                {t.labels && t.labels.length > 0 && (
                  <div className="flex flex-wrap gap-1">
                    {t.labels.map(l => (
                      <span
                        key={l.id}
                        className="text-[10px] font-bold px-1.5 py-0.5 rounded text-white"
                        style={{ backgroundColor: l.color }}
                      >
                        {l.name}
                      </span>
                    ))}
                  </div>
                )}

                <div className="flex items-start justify-between gap-2">
                  <div
                    onClick={() => onEditTask(t)}
                    className="text-xs font-bold text-slate-900 flex-1 cursor-pointer hover:text-indigo-600 transition"
                    title={UI_TEXT.TOOLTIP_TASK_DETAIL}
                  >
                    {t.title}
                  </div>

                  <div className="flex items-center gap-0.5">
                    <button
                      onClick={() => onEditTask(t)}
                      title={UI_TEXT.TOOLTIP_EDIT_TASK}
                      className="text-slate-400 hover:text-indigo-600 text-xs p-1 rounded hover:bg-indigo-50 transition"
                    >
                      ✏️
                    </button>
                    <button
                      onClick={() => onDeleteTask(t.id)}
                      title={UI_TEXT.TOOLTIP_DELETE_TASK}
                      className="text-slate-400 hover:text-red-500 text-xs p-1 rounded hover:bg-red-50 transition"
                    >
                      🗑️
                    </button>
                  </div>
                </div>

                {t.description && (
                  <p className="text-[11.5px] text-slate-500 line-clamp-2">{t.description}</p>
                )}

                {/* 2. 마감일 D-Day 뱃지 */}
                {dueInfo && (
                  <span className={`inline-flex items-center text-[10.5px] font-semibold px-1.5 py-0.5 rounded-md border w-fit ${dueInfo.className}`}>
                    {dueInfo.text}
                  </span>
                )}

                <div className="flex items-center justify-between pt-1.5 border-t border-slate-100 mt-1">
                  <div className="flex items-center gap-1.5">
                    <div className="w-5 h-5 rounded-full bg-slate-200 text-[10px] font-bold flex items-center justify-center text-slate-600">
                      {t.assigneeName ? t.assigneeName.charAt(0) : '?'}
                    </div>
                    <span className="text-xs text-slate-600 font-medium">
                      {t.assigneeName ? t.assigneeName : UI_TEXT.UNASSIGNED}
                    </span>
                  </div>

                  <div className="flex items-center gap-1">
                    {t.status !== 'TODO' && (
                      <button
                        onClick={() => onStatusChange(t, 'TODO')}
                        title={UI_TEXT.TOOLTIP_MOVE_TODO}
                        className="text-[10px] bg-slate-100 hover:bg-slate-200 text-slate-700 px-1.5 py-0.5 rounded border border-slate-300"
                      >
                        ←
                      </button>
                    )}
                    {t.status !== 'IN_PROGRESS' && (
                      <button
                        onClick={() => onStatusChange(t, 'IN_PROGRESS')}
                        title={UI_TEXT.TOOLTIP_MOVE_PROGRESS}
                        className="text-[10px] bg-slate-100 hover:bg-slate-200 text-slate-700 px-1.5 py-0.5 rounded border border-slate-300"
                      >
                        ▶
                      </button>
                    )}
                    {t.status !== 'DONE' && (
                      <button
                        onClick={() => onStatusChange(t, 'DONE')}
                        title={UI_TEXT.TOOLTIP_MOVE_DONE}
                        className="text-[10px] bg-emerald-50 hover:bg-emerald-100 text-emerald-700 px-1.5 py-0.5 rounded border border-emerald-300 font-bold"
                      >
                        ✓
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
