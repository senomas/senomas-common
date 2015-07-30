package com.senomas.common.rs;

import java.util.Arrays;

import javax.persistence.criteria.CriteriaBuilder;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

public class PageParam {
	int page;
	int size;
	String orders[];
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}

	public String[] getOrders() {
		return orders;
	}
	
	public void setOrders(String[] orders) {
		this.orders = orders;
	}
	
	public PageRequest getRequest() {
		if (size == 0) size = 100;
		if (orders == null || orders.length == 0) {
			return new PageRequest(page, size);
		} else {
			Order orders[] = new Order[this.orders.length];
			for (int i=0, il=orders.length; i<il; i++) {
				String o = this.orders[i];
				if (o.startsWith("!")) {
					orders[i] = new Order(Direction.DESC, o.substring(1));
				} else {
					orders[i] = new Order(Direction.ASC, o);
				}
			}
			return new PageRequest(page, size, new Sort(orders));
		}
	}
	
	public Class<?>[] getRoots() {
		return null;
	}
	
	public void where(CriteriaBuilder builder) {
	}

	@Override
	public String toString() {
		return "PageParam [page=" + page + ", size=" + size + ", orders="
				+ Arrays.toString(orders) + "]";
	}

}
