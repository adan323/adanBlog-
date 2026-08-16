// 博客前台 md-editor-v3 (MdPreview) 扩展资源本地化：默认从 unpkg CDN 动态加载，
// 改为自托管 public/vendor/ 下的静态资源（构建后位于 /vendor/）。
// mermaid 已全站弃用，不配置。
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
    katex: { css: `${V}katex.min.css`, js: `${V}katex.min.js` },
  },
})
