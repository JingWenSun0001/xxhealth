const escape = (v) =>
  String(v ?? "").replace(
    /[&<>"']/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[
        c
      ])
  );
export function downloadReport(r) {
  const text = `<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>${escape(
    r.reportNo
  )}</title><style>body{font:16px/1.8 sans-serif;max-width:900px;margin:40px auto;color:#14244b}h1{text-align:center;color:#1264eb}table{width:100%;border-collapse:collapse}td,th{border:1px solid #dde5f2;padding:8px;text-align:left}pre{white-space:pre-wrap;font:inherit}.bad{color:#e84e51}</style><h1>熙心健康 · 体检报告</h1><p>报告编号：${escape(
    r.reportNo
  )}　状态：${escape(r.status)}</p><p>患者：${escape(r.name)}　${escape(
    r.sex
  )}　${escape(r.age)}岁　体检日期：${escape(
    r.examDate
  )}</p><p>体检套餐：${escape(r.packageName)}　健康评分：${escape(
    r.score
  )}分</p><table><thead><tr><th>检查项目</th><th>结果</th><th>单位</th><th>参考范围</th></tr></thead><tbody>${r.items
    .map(
      (i) =>
        `<tr class="${i.abnormal ? "bad" : ""}"><td>${escape(
          i.name
        )}</td><td>${escape(i.value)}</td><td>${escape(
          i.unit
        )}</td><td>${escape(i.range)}</td></tr>`
    )
    .join("")}</tbody></table><h2>诊断结论</h2><pre>${escape(
    r.conclusion
  )}</pre><h2>医生建议</h2><pre>${escape(r.advice)}</pre><p>报告医生：${escape(
    r.doctorName
  )}　报告时间：${escape(r.reportedAt)}</p></html>`;
  const a = document.createElement("a");
  a.href = URL.createObjectURL(
    new Blob([text], { type: "text/html;charset=utf-8" })
  );
  a.download = `体检报告-${r.reportNo}.html`;
  a.click();
  setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}
