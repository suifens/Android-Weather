/*
 * Copyright (C) 2015-2016  GenGee Technology Co. Ltd. All rights reserved.
 *
 *                           http://www.GenGee.cn
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user
 * who downloaded the software, his/her employer (which must be your employer)
 * and GenGee Technology Co. Ltd (the "License").  You may not use this
 * Software unless you agree to abide by the terms of the License. The License
 * limits your use, and you acknowledge, that the Software may not be modified,
 * copied or distributed unless embedded on a GenGee Technology intelligent
 * device or system. Other than for the foregoing purpose, you may not use,
 * reproduce, copy, prepare derivative works of, modify, distribute, perform,
 * display or sell this Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * GENGEE TECHNOLOGY CO. LTD OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER
 * CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY,
 * OR OTHER LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
 * INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
 * OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
 * OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
 * (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact GenGee Technology Co. Ltd at www.GenGee.cn.
 */
package com.goodtech.tq.httpClient;


import com.goodtech.tq.R;

/**
 * 后端错误码字典
 * 
 * @author baoxiaofeng
 * 
 */
public enum ErrorCode {
	/** 请求成功 */
	SUCCESS(R.string.SUCCESS),
	/** 连接服务器失败 */
	SVRFAIL(R.string.SVRFAIL),
	/** 未知错误 */
	UNKNOWN(R.string.UNKNOWN),
	/** 请求超时 */
	TIMEOUT(R.string.TIMEOUT),
	/** 请求超时 */
	EMPTY_USER_INFO(R.string.EMPTY_USER_INFO),
	/** 数据解析错误 */
	ERRJSON(R.string.ERRJSON),
	/** 系统错误 */
	ERR000(R.string.ERR000),
	/** 未知用户 */
	ERR001(R.string.ERR001),
	/** 用户名或密码错误 */
	ERR002(R.string.ERR002),
	/** 用户授权失败 */
	ERR0003(R.string.ERR0003),
	/** 校验key不正确 */
	ERR004(R.string.ERR004),
	/** 附加特征码校验失败 */
	ERR005(R.string.ERR005),
	/** 验证码错误 */
	ERR006(R.string.ERR006),
	/** 注册用户已存在(注册请求) */
	ERR007(R.string.ERR007),
	/** 用户不存在(忘记密码请求) */
	ERR008(R.string.ERR008),
	ERR009(R.string.ERR009),
	ERR010(R.string.ERR010),
	ERR011(R.string.ERR011),
	ERR012(R.string.ERR012),
	/** 参数错误 */
	ERR400(R.string.ERR400),
	/** 授权失败，账号已过期 */
	ERR401(R.string.ERR401),
	/** 找不到指定的URL */
	ERR404(R.string.ERR404),
	/** json序列化失败或者未使用json序列化 */
	ERR405(R.string.ERR405);

	private int descriptionRes;

	private ErrorCode(int res) {
		descriptionRes = res;
	}

	public int getDescription() {
		return descriptionRes;
	}

}
