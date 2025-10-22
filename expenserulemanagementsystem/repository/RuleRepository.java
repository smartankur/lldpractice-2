package expenserulemanagementsystem.repository;

import expenserulemanagementsystem.model.Rule;

import java.util.*;

public interface RuleRepository {
    Rule save(Rule rule);
    Optional<Rule> findById(String ruleId);
    List<Rule> findAll();
    List<Rule> findAllActive();
    void deleteById(String ruleId);
    boolean existsById(String ruleId);
}
