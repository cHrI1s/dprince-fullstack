/*******************************************************************************
 * Copyright (c) 2024. AlienBase
 *
 * @project DPrince-Frontend
 * @author AlienBase
 * @copyright AlienBase All rights reserved
 ******************************************************************************/

const path = require('path');
// const webpack = require("webpack");
module.exports = {
	runtimeCompiler: true,
	configureWebpack: {
		resolve: {
			alias: {
				"@": path.resolve(__dirname, 'src')
			}
		},
		optimization: {
			splitChunks: {
				chunks: 'all'
			}
		}
	},
	chainWebpack: (config) => {
		config.resolve.extensions.merge(['.ts', '.tsx', '.vue']);
		config.module
			.rule('ts')
			.test(/\.ts$/)
			.use('ts-loader')
			.loader('ts-loader')
			.end();
		config.module
			.rule('tsx')
			.test(/\.tsx$/)
			.use('ts-loader')
			.loader('ts-loader')
			.end();
		config.plugin("html").tap((args) => {
			args[0].title = "DNote";
			args[0].appUrl = "http://localhost:8080";
			args[0].developer = "AlienBase";
			args[0].description = "DNote Application";
			return args;
		});
		/*
		config.plugin("define").tap((args)=>{
			args[0]["process.env.NODE_ENV"] = "development";
			return args;
		});*/
	},
};
