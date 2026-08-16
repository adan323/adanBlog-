// md-editor-v3 扩展资源本地化：默认从 unpkg CDN 动态加载（国内访问不稳），
// 全部改为自托管 public/vendor/ 下的静态资源（构建后位于 /admin/vendor/）。
import { config } from 'md-editor-v3'

const V = '/admin/vendor/'

// 禁用 mermaid mindmap 思维导图（效果不好，2026-08 用户明确不要）。
// 拦截方式：core 阶段把以 mindmap 开头的 mermaid fence 降级为普通代码块，
// md-editor-v3 的 renderer 只在 info==='mermaid' 时才走 mermaid 渲染，其余图不受影响。
function disableMindmap(md) {
  md.core.ruler.push('disable-mindmap', (state) => {
    for (const token of state.tokens) {
      if (
        token.type === 'fence' &&
        token.info.trim() === 'mermaid' &&
        token.content.trim().startsWith('mindmap')
      ) {
        token.info = 'text'
      }
    }
  })
}

config({
  markdownItPlugins: (plugins) => [
    ...plugins,
    { type: 'disable-mindmap', plugin: disableMindmap, options: {} },
  ],
  editorExtensions: {
    highlight: {
      js: `${V}highlight.min.js`,
      css: {
        a11y: { light: `${V}a11y-light.min.css`, dark: `${V}a11y-dark.min.css` },
        atom: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        github: { light: `${V}github.min.css`, dark: `${V}github-dark.min.css` },
        gradient: { light: `${V}gradient-light.min.css`, dark: `${V}gradient-dark.min.css` },
        kimbie: { light: `${V}kimbie-light.min.css`, dark: `${V}kimbie-dark.min.css` },
        paraiso: { light: `${V}paraiso-light.min.css`, dark: `${V}paraiso-dark.min.css` },
        qtcreator: { light: `${V}qtcreator-light.min.css`, dark: `${V}qtcreator-dark.min.css` },
        stackoverflow: { light: `${V}stackoverflow-light.min.css`, dark: `${V}stackoverflow-dark.min.css` },
      },
    },
    prettier: {
      standaloneJs: `${V}prettier.standalone.js`,
      parserMarkdownJs: `${V}prettier.markdown.js`,
    },
    screenfull: { js: `${V}screenfull.js` },
    cropper: { css: `${V}cropper.min.css`, js: `${V}cropper.min.js` },
    mermaid: { js: `${V}mermaid.min.js`, enableZoom: true },
    katex: { css: `${V}katex.min.css`, js: `${V}katex.min.js` },
  },
})
