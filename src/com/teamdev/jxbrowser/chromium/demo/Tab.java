/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

package com.teamdev.jxbrowser.chromium.demo;

/**
 * @author TeamDev Ltd.
 */
public class Tab {

	private TabCaption caption;
	private TabContent content;
	private boolean closeable = true;

	public Tab(TabCaption caption, TabContent content) {
		this(caption,content, true);
	}
	
	public Tab(TabCaption caption, TabContent content, boolean closeable) {
		super();
		this.caption = caption;
		this.content = content;
		this.closeable = closeable;
	}

	public TabCaption getCaption() {
		return caption;
	}

	public TabContent getContent() {
		return content;
	}

	public void setContent(TabContent content) {
		this.content = content;
	}

	public boolean isCloseable() {
		return closeable;
	}

	public void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

}
