package com.sunweisheng.k8sdeployingjava.demo;

import com.sunweisheng.k8sdeployingjava.web.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoRecordService {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    );

    private final DemoRecordRepository repository;

    public DemoRecordService(DemoRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<DemoRecordResponse> list(int page, int size) {
        Page<DemoRecordResponse> result = repository.findAll(PageRequest.of(page, size, DEFAULT_SORT))
                .map(DemoRecordResponse::from);
        return PagedResponse.from(result);
    }

    @Transactional
    public DemoRecordResponse create(DemoRecordRequest request) {
        DemoRecord record = new DemoRecord(normalize(request.title()), normalize(request.content()));
        return DemoRecordResponse.from(repository.save(record));
    }

    @Transactional
    public DemoRecordResponse update(long id, DemoRecordRequest request) {
        DemoRecord record = findRecord(id);
        record.update(normalize(request.title()), normalize(request.content()));
        return DemoRecordResponse.from(record);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(findRecord(id));
    }

    private DemoRecord findRecord(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("记录不存在或已被删除"));
    }

    private String normalize(String value) {
        return value.strip();
    }
}
