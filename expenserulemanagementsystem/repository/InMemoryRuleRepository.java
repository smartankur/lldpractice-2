package expenserulemanagementsystem.repository;

import expenserulemanagementsystem.model.Rule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryRuleRepository implements RuleRepository {
    private final Map<String, Rule> storage = new ConcurrentHashMap<>();
    
    @Override
    public Rule save(Rule rule) {
        storage.put(rule.getRuleId(), rule);
        return rule;
    }
    
    @Override
    public Optional<Rule> findById(String ruleId) {
        return Optional.ofNullable(storage.get(ruleId));
    }
    
    @Override
    public List<Rule> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public List<Rule> findAllActive() {
        return storage.values().stream()
            .filter(Rule::isActive)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(String ruleId) {
        storage.remove(ruleId);
    }
    
    @Override
    public boolean existsById(String ruleId) {
        return storage.containsKey(ruleId);
    }
}