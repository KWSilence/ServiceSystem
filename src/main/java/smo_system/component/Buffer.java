package smo_system.component;

import smo_system.util.TakeUtil;

import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private final int capacity;
    private final List<Request> list;
    private final List<Request> requestsPackage;
    private int takeIndex;

    public Buffer(int capacity) {
        this.capacity = capacity;
        this.list = new ArrayList<>();
        this.requestsPackage = new ArrayList<>();
        this.takeIndex = 0;
    }


    public Buffer(Buffer buffer) {
        this.capacity = buffer.capacity;
        this.list = buffer.list.stream().map(r -> TakeUtil.transformOrNull(r, Request::new)).toList();
        this.requestsPackage = buffer.requestsPackage.stream().map(r -> TakeUtil.transformOrNull(r, Request::new)).toList();
        this.takeIndex = buffer.takeIndex;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean isFull() {
        return (list.size() == capacity);
    }

    public int getTakeIndex() {
        return takeIndex;
    }

    public int getSize() {
        return list.size();
    }

    public List<Request> getList() {
        return list;
    }

    public List<Request> getRequestsPackage() {
        return requestsPackage;
    }

    public int getCapacity() {
        return capacity;
    }

    public void putRequest(Request request) {
        list.add(request);
    }

    public Request getRequest() {
        return isEmpty() ? null : getPriorityRequest();
    }

    public void createPackage() {
        if (requestsPackage.isEmpty() && !list.isEmpty()) {
            int priority = list.get(0).getSourceNumber();
            for (Request request : list) {
                int current = request.getSourceNumber();
                if (priority == current) {
                    requestsPackage.add(request);
                }
                if (priority > current) {
                    priority = current;
                    requestsPackage.clear();
                    requestsPackage.add(request);
                }
            }
        }
    }

    private Request getPriorityRequest() {
        createPackage();
        Request request = requestsPackage.remove(0);
        takeIndex = list.indexOf(request);
        list.remove(takeIndex);
        return request;
    }
}
