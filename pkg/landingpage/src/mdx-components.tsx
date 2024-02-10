import type { MDXComponents } from 'mdx/types'

export function useMDXComponents(components: MDXComponents): MDXComponents {
	return {
		p: ({ children }) => <p style={{ fontSize: '20px', padding: '0px 20px 20px 20px' }}>{children}</p>,
		a: ({ children }) => (
			<a style={{ textDecoration: 'underline', fontSize: '20px', padding: '20px' }}>{children}</a>
		),
		li: ({ children }) => (
			<li style={{ fontStyle: 'italic', fontSize: '20px', padding: '10px 20px 20px 20px' }}>{children}</li>
		),
		...components
	}
}
