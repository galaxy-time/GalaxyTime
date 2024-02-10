const withMDX = require('@next/mdx')({
	extension: /\.mdx?$/
})

/** @type {import('next').NextConfig} */

const nextConfig = {
	pageExtensions: ['js', 'jsx', 'mdx', 'ts', 'tsx'],
	typescript: { ignoreBuildErrors: true }
}

module.exports = withMDX(nextConfig)
