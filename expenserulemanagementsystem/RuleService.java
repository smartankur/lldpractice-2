package expenserulemanagementsystem;

import expenserulemanagementsystem.model.*;
import expenserulemanagementsystem.repository.RuleRepository;

import java.util.List;
import java.util.stream.Collectors;

public class RuleService {
    private final RuleRepository ruleRepository;
    
    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }
    
    public RuleResponse createRule(RuleCreateRequest request) {
        validateCreateRequest(request);
        Rule rule = RuleMapper.toEntity(request);
        Rule savedRule = ruleRepository.save(rule);
        return RuleMapper.toResponse(savedRule);
    }
    
    public RuleResponse updateRule(String ruleId, RuleUpdateRequest request) {
        Rule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        
        RuleMapper.updateEntity(rule, request);
        Rule updatedRule = ruleRepository.save(rule);
        return RuleMapper.toResponse(updatedRule);
    }
    
    public RuleResponse getRuleById(String ruleId) {
        Rule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        return RuleMapper.toResponse(rule);
    }
    
    public List<RuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
            .map(RuleMapper::toResponse)
            .collect(Collectors.toList());
    }
    
    public List<Rule> getAllActiveRules() {
        return ruleRepository.findAllActive();
    }
    
    public void deleteRule(String ruleId) {
        if (!ruleRepository.existsById(ruleId)) {
            throw new RuntimeException("Rule not found: " + ruleId);
        }
        ruleRepository.deleteById(ruleId);
    }
    
    public void deactivateRule(String ruleId) {
        Rule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        rule.setActive(false);
        ruleRepository.save(rule);
    }
    
    private void validateCreateRequest(RuleCreateRequest request) {
        if (request.getRuleName() == null || request.getRuleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name is required");
        }
        if (request.getRuleType() == null) {
            throw new IllegalArgumentException("Rule type is required");
        }
        try {
            RuleType.valueOf(request.getRuleType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid rule type: " + request.getRuleType());
        }
    }
}