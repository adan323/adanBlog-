// 博客前台 md-editor-v3 (MdPreview) 扩展资源本地化：默认从 unpkg CDN 动态加载，
// 改为自托管 public/vendor/ 下的静态资源（构建后位于 /vendor/）。
// 启用完整扩展：mermaid 图、katex 公式、echarts 图表、highlight 高亮。
import { config } from 'md-editor-v3'

const V = '/vendor/'

config({
  editorExtensions: {
    highlight: {
      js: `${V}highlight.min.js`,
      css: {
        atom: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        github: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        a11y: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        gradient: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        kimbie: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        paraiso: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        qtcreator: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
        stackoverflow: { light: `${V}atom-one-light.min.css`, dark: `${V}atom-one-dark.min.css` },
      },
    },
    mermaid: { js: `${V}mermaid.min.js`, enableZoom: false },
    katex: { css: `${V}katex.min.css`, js: `${V}katex.min.js` },
    echarts: {
      js: `${V}echarts.min.js`,
      parseOption: (code) => new Function(`return ${code}`)(),
    },
  },
})
