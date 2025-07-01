https://web.stanford.edu/class/datasci112/lectures/lecture6.pdf

https://archive.uea.ac.uk/jtm/contents.htm


import java.util.*;

public class EmployeeReportAnalyzer {
    public static class Result {
        public final int directReports;
        public final int totalReports;
        
        public Result(int direct, int total) {
            this.directReports = direct;
            this.totalReports = total;
        }
        
        @Override
        public String toString() {
            return "{direct=" + directReports + ", total=" + totalReports + "}";
        }
    }

    private final Map<String, List<String>> orgStructure;
    private final Map<String, Result> resultCache;

    public EmployeeReportAnalyzer(Map<String, List<String>> orgStructure) {
        this.orgStructure = orgStructure;
        this.resultCache = new HashMap<>();
    }

    public Map<String, Result> analyzeReports(String rootEmployee) {
        if (!orgStructure.containsKey(rootEmployee)) {
            return Map.of(rootEmployee, new Result(0, 0));
        }
        
        // Process the entire connected component
        processEmployee(rootEmployee);
        
        // Collect all related employees (those in the cache)
        Map<String, Result> results = new HashMap<>();
        for (String employee : resultCache.keySet()) {
            results.put(employee, resultCache.get(employee));
        }
        
        return results;
    }

    private Result processEmployee(String employee) {
        if (resultCache.containsKey(employee)) {
            return resultCache.get(employee);
        }

        List<String> directReports = orgStructure.getOrDefault(employee, Collections.emptyList());
        int totalReports = directReports.size();
        Set<String> allReports = new HashSet<>(directReports);

        for (String report : directReports) {
            Result subResult = processEmployee(report);
            allReports.addAll(getAllReports(report));
            totalReports = allReports.size();
        }

        Result result = new Result(directReports.size(), totalReports);
        resultCache.put(employee, result);
        return result;
    }

    private Set<String> getAllReports(String employee) {
        Set<String> reports = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(employee);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> direct = orgStructure.getOrDefault(current, Collections.emptyList());
            for (String report : direct) {
                if (reports.add(report)) {
                    queue.add(report);
                }
            }
        }
        return reports;
    }

    public static void main(String[] args) {
        Map<String, List<String>> org = new HashMap<>();
        org.put("James", Arrays.asList("paul", "jon", "doe", "peter", "david"));
        org.put("peter", Arrays.asList("jon", "doe", "name1", "name2"));
        org.put("david", Arrays.asList("name3", "name4"));
        org.put("name1", Collections.emptyList());
        org.put("name2", Collections.emptyList());
        org.put("name3", Collections.emptyList());
        org.put("name4", Collections.emptyList());

        EmployeeReportAnalyzer analyzer = new EmployeeReportAnalyzer(org);
        Map<String, Result> results = analyzer.analyzeReports("James");
        
        System.out.println("Report Analysis:");
        results.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}






















import java.util.*;

public class EmployeeReportCounter {
    private Map<String, List<String>> employeeReports;
    private Map<String, Integer> countCache;

    public EmployeeReportCounter(Map<String, List<String>> employeeReports) {
        this.employeeReports = employeeReports;
        this.countCache = new HashMap<>();
    }

    public Map<String, Integer> calculateAllReportCounts() {
        for (String employee : employeeReports.keySet()) {
            if (!countCache.containsKey(employee)) {
                calculateReportCount(employee);
            }
        }
        return new HashMap<>(countCache);
    }

    private void calculateReportCount(String rootEmployee) {
        Set<String> uniqueReports = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        queue.add(rootEmployee);
        uniqueReports.add(rootEmployee); // Prevent self-counting

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> reports = employeeReports.getOrDefault(current, Collections.emptyList());

            for (String report : reports) {
                if (countCache.containsKey(report)) {
                    // Merge cached counts without expanding
                    uniqueReports.add(report);
                    uniqueReports.addAll(getAllCachedReports(report));
                } else if (uniqueReports.add(report)) {
                    queue.add(report);
                }
            }
        }
        
        // Store only the count (-1 to exclude self)
        countCache.put(rootEmployee, uniqueReports.size() - 1);
    }

    private Set<String> getAllCachedReports(String employee) {
        Set<String> reports = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(employee);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> directReports = employeeReports.getOrDefault(current, Collections.emptyList());

            for (String report : directReports) {
                if (reports.add(report)) {
                    queue.add(report);
                }
            }
        }
        return reports;
    }

    public static void main(String[] args) {
        Map<String, List<String>> input = new HashMap<>();
        input.put("James", Arrays.asList("paul", "jon", "doe", "peter", "david", "name9", "name10", "name12", "name14"));
        input.put("peter", Arrays.asList("jon", "doe", "david", "name1", "name2", "name3", "name4"));
        input.put("david", Arrays.asList("jon", "doe", "peter", "name1", "name2", "name3", "name4", "name5"));

        EmployeeReportCounter counter = new EmployeeReportCounter(input);
        Map<String, Integer> results = counter.calculateAllReportCounts();

        System.out.println("Report Counts:");
        results.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}



















import java.util.*;

public class TriangulCalc {
    private Map<String, List<String>> employeeManagers;
    private Map<String, Integer> triganceCache;

    public TriangulCalc(Map<String, List<String>> employeeManagers) {
        this.employeeManagers = employeeManagers;
        this.triganceCache = new HashMap<>();
    }

    public int calculateTrigance(String employee) {
        // Check cache first
        if (triganceCache.containsKey(employee)) {
            return triganceCache.get(employee);
        }

        Stack<String> stack = new Stack<>();
        stack.push(employee);
        Map<String, Integer> tempCounts = new HashMap<>();
        tempCounts.put(employee, 0);

        while (!stack.isEmpty()) {
            String current = stack.peek();
            List<String> managers = employeeManagers.getOrDefault(current, Collections.emptyList());

            // Check if all managers are processed
            boolean allProcessed = true;
            int managerSum = 0;
            
            for (String manager : managers) {
                if (!triganceCache.containsKey(manager) && !tempCounts.containsKey(manager)) {
                    stack.push(manager);
                    tempCounts.put(manager, 0);
                    allProcessed = false;
                    break;
                }
            }

            if (allProcessed) {
                stack.pop(); // Done with this employee
                int total = managers.size();
                for (String manager : managers) {
                    total += triganceCache.containsKey(manager) ? 
                            triganceCache.get(manager) : tempCounts.get(manager);
                }
                tempCounts.put(current, total);
                
                // Move to cache once fully calculated
                if (current.equals(employee)) {
                    triganceCache.put(current, total);
                }
            }
        }

        return triganceCache.get(employee);
    }

    public Map<String, Integer> calculateAllTrigances() {
        Map<String, Integer> result = new HashMap<>();
        for (String employee : employeeManagers.keySet()) {
            result.put(employee, calculateTrigance(employee));
        }
        return result;
    }

    public static void main(String[] args) {
        // Test with a deep hierarchy
        Map<String, List<String>> input = new HashMap<>();
        input.put("A", Arrays.asList("B", "C"));
        input.put("B", Arrays.asList("D", "E"));
        input.put("C", Arrays.asList("F"));
        input.put("D", Arrays.asList("G"));
        input.put("E", Collections.emptyList());
        input.put("F", Arrays.asList("E", "G"));
        input.put("G", Collections.emptyList());

        TriangulCalc calculator = new TriangulCalc(input);
        Map<String, Integer> triganceValues = calculator.calculateAllTrigances();

        System.out.println("Employee Trigance Values:");
        for (Map.Entry<String, Integer> entry : triganceValues.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
}


























import java.util.*;

public class TriangulCalc {
    private Map<String, List<String>> employeeManagers;
    private Map<String, Integer> triganceCache;

    public TriangulCalc(Map<String, List<String>> employeeManagers) {
        this.employeeManagers = employeeManagers;
        this.triganceCache = new HashMap<>();
    }

    public int calculateTrigance(String employee) {
        // Check if we've already calculated this employee's trigance
        if (triganceCache.containsKey(employee)) {
            return triganceCache.get(employee);
        }

        // Get the employee's direct managers (empty list if none)
        List<String> managers = employeeManagers.getOrDefault(employee, Collections.emptyList());
        int total = managers.size(); // Start with direct managers count

        // Recursively add trigance of each manager
        for (String manager : managers) {
            total += calculateTrigance(manager);
        }

        // Cache the result before returning
        triganceCache.put(employee, total);
        return total;
    }

    public Map<String, Integer> calculateAllTrigances() {
        Map<String, Integer> result = new HashMap<>();
        for (String employee : employeeManagers.keySet()) {
            result.put(employee, calculateTrigance(employee));
        }
        return result;
    }

    public static void main(String[] args) {
        // Sample input from the problem statement
        Map<String, List<String>> input = new HashMap<>();
        input.put("James", Arrays.asList("paul", "ade", "bola", "olu"));
        input.put("bola", Arrays.asList("ade", "olu", "donald"));
        input.put("paul", Arrays.asList("bola", "olu", "jones"));
        input.put("ade", Arrays.asList("ola", "femi", "bola", "olu"));
        input.put("donald", Arrays.asList("paul", "ade", "bola", "olu"));
        // Adding base cases (employees with no managers)
        input.put("ola", Collections.emptyList());
        input.put("femi", Collections.emptyList());
        input.put("olu", Collections.emptyList());
        input.put("jones", Collections.emptyList());

        TriangulCalc calculator = new TriangulCalc(input);
        Map<String, Integer> triganceValues = calculator.calculateAllTrigances();

        // Print results
        System.out.println("Employee Trigance Values:");
        for (Map.Entry<String, Integer> entry : triganceValues.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        /* Expected Output (based on the recursive calculation):
           James = 21
           bola = 12
           paul = 10
           ade = 8
           donald = 21
           ola = 0
           femi = 0
           olu = 0
           jones = 0
        */
    }
}



import java.util.*;

public class TriangulCalc {
    
    private Map<String, List<String>> employeeManagers;
    private Map<String, Long> memoizedResults;
    
    public TriangulCalc() {
        this.employeeManagers = new HashMap<>();
        this.memoizedResults = new HashMap<>();
    }
    
    /**
     * Add an employee with their direct managers
     */
    public void addEmployee(String employee, List<String> managers) {
        employeeManagers.put(employee, new ArrayList<>(managers));
    }
    
    /**
     * Calculate triangul-value for a specific employee
     */
    public long calculateTriangulValue(String employee) {
        // Return memoized result if already calculated
        if (memoizedResults.containsKey(employee)) {
            return memoizedResults.get(employee);
        }
        
        // Get direct managers
        List<String> directManagers = employeeManagers.getOrDefault(employee, new ArrayList<>());
        
        // Base case: no managers
        if (directManagers.isEmpty()) {
            memoizedResults.put(employee, 0L);
            return 0L;
        }
        
        // Calculate: direct managers count + sum of all managers' triangul-values
        long triangulValue = directManagers.size(); // Count direct managers
        
        // Add triangul-value of each direct manager recursively
        for (String manager : directManagers) {
            triangulValue += calculateTriangulValue(manager);
        }
        
        // Memoize and return result
        memoizedResults.put(employee, triangulValue);
        return triangulValue;
    }
    
    /**
     * Calculate triangul-values for all employees
     */
    public Map<String, Long> calculateAllTriangulValues() {
        Map<String, Long> results = new HashMap<>();
        
        for (String employee : employeeManagers.keySet()) {
            results.put(employee, calculateTriangulValue(employee));
        }
        
        return results;
    }
    
    /**
     * Optimized version using topological sorting for better performance with large datasets
     */
    public Map<String, Long> calculateAllTriangulValuesOptimized() {
        Map<String, Long> results = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        
        // Initialize all employees
        for (String employee : employeeManagers.keySet()) {
            inDegree.put(employee, 0);
        }
        
        // Calculate in-degrees (how many people manage this person)
        for (String employee : employeeManagers.keySet()) {
            for (String manager : employeeManagers.get(employee)) {
                inDegree.put(manager, inDegree.getOrDefault(manager, 0) + 1);
            }
        }
        
        // Find employees with no subordinates (leaf nodes in management hierarchy)
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }
        
        // Process employees in topological order
        while (!queue.isEmpty()) {
            String employee = queue.poll();
            
            // Calculate triangul-value for current employee
            List<String> directManagers = employeeManagers.getOrDefault(employee, new ArrayList<>());
            long triangulValue = directManagers.size();
            
            for (String manager : directManagers) {
                triangulValue += results.getOrDefault(manager, 0L);
            }
            
            results.put(employee, triangulValue);
            
            // Update in-degrees for employees who have this person as manager
            for (String otherEmployee : employeeManagers.keySet()) {
                if (employeeManagers.get(otherEmployee).contains(employee)) {
                    inDegree.put(otherEmployee, inDegree.get(otherEmployee) - 1);
                    if (inDegree.get(otherEmployee) == 0) {
                        queue.offer(otherEmployee);
                    }
                }
            }
        }
        
        return results;
    }
    
    public static void main(String[] args) {
        TriangulCalc calc = new TriangulCalc();
        
        // Sample test data from the problem
        calc.addEmployee("james", Arrays.asList("paul", "ade", "bola", "olu"));
        calc.addEmployee("bola", Arrays.asList("ade", "olu", "donald"));
        calc.addEmployee("paul", Arrays.asList("bola", "olu", "jones"));
        calc.addEmployee("ade", Arrays.asList("ola", "femi", "bola", "olu"));
        calc.addEmployee("donald", Arrays.asList("paul", "ade", "bola", "olu"));
        
        // Employees with no managers (leaf nodes)
        calc.addEmployee("olu", new ArrayList<>());
        calc.addEmployee("jones", new ArrayList<>());
        calc.addEmployee("ola", new ArrayList<>());
        calc.addEmployee("femi", new ArrayList<>());
        
        System.out.println("=== Triangul-Calc Results ===");
        
        // Calculate using memoized recursive approach
        System.out.println("\n--- Using Recursive Memoization ---");
        Map<String, Long> results = calc.calculateAllTriangulValues();
        
        for (Map.Entry<String, Long> entry : results.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        // Reset for optimized calculation
        calc.memoizedResults.clear();
        
        System.out.println("\n--- Using Optimized Topological Sort ---");
        Map<String, Long> optimizedResults = calc.calculateAllTriangulValuesOptimized();
        
        for (Map.Entry<String, Long> entry : optimizedResults.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        // Detailed calculation for James (for verification)
        System.out.println("\n--- Detailed Calculation for James ---");
        System.out.println("James has direct managers: [paul, ade, bola, olu] = 4");
        System.out.println("Paul's triangul-value: " + results.get("paul"));
        System.out.println("Ade's triangul-value: " + results.get("ade"));
        System.out.println("Bola's triangul-value: " + results.get("bola"));
        System.out.println("Olu's triangul-value: " + results.get("olu"));
        System.out.println("James total: 4 + " + results.get("paul") + " + " + results.get("ade") + 
                          " + " + results.get("bola") + " + " + results.get("olu") + " = " + results.get("james"));
        
        // Performance test simulation
        System.out.println("\n--- Performance Simulation ---");
        long startTime = System.currentTimeMillis();
        
        // Simulate larger dataset
        TriangulCalc largeSim = new TriangulCalc();
        for (int i = 0; i < 10000; i++) {
            List<String> managers = new ArrayList<>();
            // Each employee has 2-5 random managers
            int numManagers = 2 + (i % 4);
            for (int j = 0; j < numManagers; j++) {
                managers.add("manager_" + ((i + j) % 1000));
            }
            largeSim.addEmployee("employee_" + i, managers);
        }
        
        // Add some leaf nodes
        for (int i = 0; i < 1000; i++) {
            largeSim.addEmployee("manager_" + i, new ArrayList<>());
        }
        
        Map<String, Long> largeResults = largeSim.calculateAllTriangulValues();
        long endTime = System.currentTimeMillis();
        
        System.out.println("Processed 11,000 employees in " + (endTime - startTime) + " ms");
        System.out.println("Sample result - employee_0: " + largeResults.get("employee_0"));
        System.out.println("Sample result - employee_100: " + largeResults.get("employee_100"));
    }
}

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Data Statistics Dashboard</title>
    <style>
        /* Reset and Base Styles */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
        }
        
        body {
            background-color: #f1f5f9;
            color: #334155;
            line-height: 1.6;
            padding: 20px;
        }
        
        /* Container Styling */
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        /* Dashboard Styling */
        .dashboard {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }
        
        /* Dashboard Header */
        .dashboard-header {
            background-color: #2563eb;
            color: white;
            padding: 24px 32px;
        }
        
        .dashboard-title {
            font-size: 24px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        
        .dashboard-subtitle {
            font-size: 14px;
            opacity: 0.9;
        }
        
        /* Stats Grid Layout */
        .stats-container {
            padding: 32px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 24px;
        }
        
        /* Individual Stat Cards */
        .stat-card {
            background-color: #f8fafc;
            border-radius: 8px;
            padding: 24px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            position: relative;
            overflow: hidden;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
        }
        
        /* Color indicators on cards */
        .stat-card::before {
            content: "";
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
        }
        
        .total::before {
            background-color: #2563eb;
        }
        
        .deleted::before {
            background-color: #dc2626;
        }
        
        .updated::before {
            background-color: #ca8a04;
        }
        
        /* Stat Headers */
        .stat-title {
            font-size: 14px;
            font-weight: 500;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 16px;
        }
        
        /* Stat Values */
        .stat-value {
            font-size: 32px;
            font-weight: 700;
            margin-bottom: 8px;
        }
        
        .stat-description {
            font-size: 14px;
            color: #64748b;
            margin-bottom: 16px;
        }
        
        /* Progress Bars */
        .progress-bar {
            height: 8px;
            background-color: #e2e8f0;
            border-radius: 4px;
            overflow: hidden;
            margin-top: 16px;
        }
        
        .progress {
            height: 100%;
            border-radius: 4px;
        }
        
        .total .progress {
            background-color: #2563eb;
            width: 100%;
        }
        
        .deleted .progress {
            background-color: #dc2626;
            width: 50%;
        }
        
        .updated .progress {
            background-color: #ca8a04;
            width: 20.4%;
        }
        
        /* Dashboard Footer */
        .dashboard-footer {
            padding: 24px 32px;
            border-top: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 14px;
            color: #64748b;
        }
        
        /* Responsive Design */
        @media (max-width: 768px) {
            .container {
                padding: 10px;
            }
            
            .stats-container {
                grid-template-columns: 1fr;
                padding: 16px;
            }
            
            .dashboard-header,
            .dashboard-footer {
                padding: 16px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="dashboard">
            <div class="dashboard-header">
                <h1 class="dashboard-title">Data Statistics Dashboard</h1>
                <p class="dashboard-subtitle">Overview of your database records</p>
            </div>
            
            <div class="stats-container">
                <div class="stat-card total">
                    <h2 class="stat-title">Total Records</h2>
                    <div class="stat-value">500</div>
                    <p class="stat-description">Total number of records in database</p>
                    <div class="progress-bar">
                        <div class="progress"></div>
                    </div>
                </div>
                
                <div class="stat-card deleted">
                    <h2 class="stat-title">Deleted Records</h2>
                    <div class="stat-value">250</div>
                    <p class="stat-description">50% of total records deleted</p>
                    <div class="progress-bar">
                        <div class="progress"></div>
                    </div>
                </div>
                
                <div class="stat-card updated">
                    <h2 class="stat-title">Updated Records</h2>
                    <div class="stat-value">102</div>
                    <p class="stat-description">20.4% of total records updated</p>
                    <div class="progress-bar">
                        <div class="progress"></div>
                    </div>
                </div>
            </div>
            
            <div class="dashboard-footer">
                <div>Last updated: April 15, 2025</div>
                <div>Data Management System v1.0</div>
            </div>
        </div>
    </div>
</body>
</html>
























<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Data Statistics Dashboard</title>
    <style>
        :root {
            --primary: #2563eb;
            --secondary: #64748b;
            --danger: #dc2626;
            --success: #16a34a;
            --warning: #ca8a04;
            --bg-light: #f8fafc;
            --shadow: rgba(0, 0, 0, 0.1);
        }
        
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
        }
        
        body {
            background-color: #f1f5f9;
            color: #334155;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 2rem;
        }
        
        .dashboard {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 4px 6px var(--shadow);
            overflow: hidden;
        }
        
        .dashboard-header {
            background-color: var(--primary);
            color: white;
            padding: 1.5rem 2rem;
        }
        
        .dashboard-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
        }
        
        .dashboard-subtitle {
            font-size: 0.9rem;
            opacity: 0.9;
        }
        
        .stats-container {
            padding: 2rem;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
        }
        
        .stat-card {
            background-color: var(--bg-light);
            border-radius: 8px;
            padding: 1.5rem;
            box-shadow: 0 2px 4px var(--shadow);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 6px 12px var(--shadow);
        }
        
        .stat-header {
            margin-bottom: 1rem;
        }
        
        .stat-title {
            font-size: 0.9rem;
            font-weight: 500;
            color: var(--secondary);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .stat-indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            margin-right: 10px;
            display: inline-block;
        }
        
        .stat-indicator.total {
            background-color: var(--primary);
        }
        
        .stat-indicator.deleted {
            background-color: var(--danger);
        }
        
        .stat-indicator.updated {
            background-color: var(--warning);
        }
        
        .stat-value {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
        }
        
        .stat-description {
            font-size: 0.875rem;
            color: var(--secondary);
        }
        
        .progress-bar {
            height: 8px;
            background-color: #e2e8f0;
            border-radius: 4px;
            margin-top: 1rem;
            overflow: hidden;
        }
        
        .progress {
            height: 100%;
            border-radius: 4px;
        }
        
        .progress.total {
            background-color: var(--primary);
            width: 100%;
        }
        
        .progress.deleted {
            background-color: var(--danger);
            width: 50%;
        }
        
        .progress.updated {
            background-color: var(--warning);
            width: 20.4%;
        }
        
        .dashboard-footer {
            padding: 1.5rem 2rem;
            border-top: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 0.875rem;
            color: var(--secondary);
        }
        
        @media (max-width: 768px) {
            .container {
                padding: 1rem;
            }
            
            .stats-container {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="dashboard">
            <div class="dashboard-header">
                <h1 class="dashboard-title">Data Statistics Dashboard</h1>
                <p class="dashboard-subtitle">Overview of your database records</p>
            </div>
            
            <div class="stats-container">
                <div class="stat-card">
                    <div class="stat-header">
                        <h2 class="stat-title"><span class="stat-indicator total"></span>Total Records</h2>
                    </div>
                    <div class="stat-value">500</div>
                    <p class="stat-description">Total number of records in database</p>
                    <div class="progress-bar">
                        <div class="progress total"></div>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="stat-header">
                        <h2 class="stat-title"><span class="stat-indicator deleted"></span>Deleted Records</h2>
                    </div>
                    <div class="stat-value">250</div>
                    <p class="stat-description">50% of total records deleted</p>
                    <div class="progress-bar">
                        <div class="progress deleted"></div>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="stat-header">
                        <h2 class="stat-title"><span class="stat-indicator updated"></span>Updated Records</h2>
                    </div>
                    <div class="stat-value">102</div>
                    <p class="stat-description">20.4% of total records updated</p>
                    <div class="progress-bar">
                        <div class="progress updated"></div>
                    </div>
                </div>
            </div>
            
            <div class="dashboard-footer">
                <div>Last updated: April 15, 2025</div>
                <div>Data Management System v1.0</div>
            </div>
        </div>
    </div>
</body>
</html>














import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonPostClient {

    /**
     * Sends a POST request with JSON payload to the specified URL.
     * 
     * @param urlString The URL to send the request to
     * @param jsonPayload The JSON payload as a string
     * @return The response from the server as a string
     * @throws IOException If an I/O error occurs
     */
    public static String postJson(String urlString, String jsonPayload) throws IOException {
        // Create URL object
        URL url = new URL(urlString);
        
        // Open connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // Set up the connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            // Write JSON data to the connection
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check the response code
            int responseCode = conn.getResponseCode();
            
            // Read the response (from either input or error stream)
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        conn.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            // Build the response string
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            return response.toString();
        } finally {
            // Always disconnect
            conn.disconnect();
        }
    }
    
    // Example usage
    public static void main(String[] args) {
        try {
            String endpoint = "https://jsonplaceholder.typicode.com/posts";
            String payload = "{\"title\":\"Test Title\",\"body\":\"Test Body\",\"userId\":1}";
            
            String response = postJson(endpoint, payload);
            System.out.println("Response: " + response);
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}















import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * A robust and scalable HTTP client for making REST API calls
 */
public class HttpClient {
    
    // Configurable properties
    private final int connectTimeout;
    private final int readTimeout;
    private final int maxRetries;
    private final long retryDelayMs;
    private final ExecutorService executorService;
    private final boolean enableCompression;
    
    // Default headers
    private final Map<String, String> defaultHeaders;
    
    /**
     * Builder for creating HttpClient instances
     */
    public static class Builder {
        private int connectTimeout = 10000; // 10 seconds
        private int readTimeout = 30000; // 30 seconds
        private int maxRetries = 3;
        private long retryDelayMs = 1000; // 1 second
        private int threadPoolSize = 10;
        private boolean enableCompression = true;
        private Map<String, String> defaultHeaders = new HashMap<>();
        
        public Builder() {
            // Add default headers
            defaultHeaders.put("Content-Type", "application/json");
            defaultHeaders.put("Accept", "application/json");
        }
        
        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = (int) timeout.toMillis();
            return this;
        }
        
        public Builder readTimeout(Duration timeout) {
            this.readTimeout = (int) timeout.toMillis();
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public Builder retryDelay(Duration delay) {
            this.retryDelayMs = delay.toMillis();
            return this;
        }
        
        public Builder threadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }
        
        public Builder enableCompression(boolean enable) {
            this.enableCompression = enable;
            return this;
        }
        
        public Builder withDefaultHeader(String key, String value) {
            this.defaultHeaders.put(key, value);
            return this;
        }
        
        public HttpClient build() {
            return new HttpClient(this);
        }
    }
    
    /**
     * Response object containing HTTP response details
     */
    public static class HttpResponse {
        private final int statusCode;
        private final String body;
        private final Map<String, String> headers;
        
        public HttpResponse(int statusCode, String body, Map<String, String> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public String getBody() {
            return body;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        @Override
        public String toString() {
            return "HttpResponse{" +
                    "statusCode=" + statusCode +
                    ", body='" + body + '\'' +
                    ", headers=" + headers +
                    '}';
        }
    }
    
    /**
     * Private constructor - use Builder to create instances
     */
    private HttpClient(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.maxRetries = builder.maxRetries;
        this.retryDelayMs = builder.retryDelayMs;
        this.executorService = Executors.newFixedThreadPool(builder.threadPoolSize);
        this.enableCompression = builder.enableCompression;
        this.defaultHeaders = builder.defaultHeaders;
    }
    
    /**
     * Send a POST request with JSON payload synchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @return HttpResponse object containing the response
     * @throws IOException If an I/O error occurs
     */
    public HttpResponse post(String url, String jsonPayload) throws IOException {
        return post(url, jsonPayload, null);
    }
    
    /**
     * Send a POST request with JSON payload and custom headers synchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @param headers Additional headers (will override default headers)
     * @return HttpResponse object containing the response
     * @throws IOException If an I/O error occurs
     */
    public HttpResponse post(String url, String jsonPayload, Map<String, String> headers) throws IOException {
        Map<String, String> mergedHeaders = new HashMap<>(defaultHeaders);
        if (headers != null) {
            mergedHeaders.putAll(headers);
        }
        
        if (enableCompression) {
            mergedHeaders.put("Accept-Encoding", "gzip");
        }
        
        IOException lastException = null;
        
        // Retry logic
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    // Exponential backoff with jitter
                    long delay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                    delay += (long) (delay * 0.2 * Math.random()); // Add 0-20% jitter
                    Thread.sleep(delay);
                }
                
                return executeRequest("POST", url, jsonPayload, mergedHeaders);
                
            } catch (IOException e) {
                lastException = e;
                
                // Only retry on connection issues, not HTTP errors
                if (!(e instanceof ConnectException || e instanceof SocketTimeoutException)) {
                    throw e;
                }
                
                System.err.println("Request failed (attempt " + (attempt + 1) + "/" + (maxRetries + 1) + 
                        "): " + e.getMessage());
                
                if (attempt == maxRetries) {
                    System.err.println("Max retries reached. Giving up.");
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
        
        // Should never reach here due to the throw in the loop
        throw lastException;
    }
    
    /**
     * Send a POST request asynchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @return CompletableFuture that will complete with the HttpResponse
     */
    public CompletableFuture<HttpResponse> postAsync(String url, String jsonPayload) {
        return postAsync(url, jsonPayload, null);
    }
    
    /**
     * Send a POST request with custom headers asynchronously
     * 
     * @param url The endpoint URL
     * @param jsonPayload The JSON payload as string
     * @param headers Additional headers (will override default headers)
     * @return CompletableFuture that will complete with the HttpResponse
     */
    public CompletableFuture<HttpResponse> postAsync(String url, String jsonPayload, Map<String, String> headers) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return post(url, jsonPayload, headers);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute request: " + e.getMessage(), e);
            }
        }, executorService);
    }
    
    /**
     * Execute the actual HTTP request
     */
    private HttpResponse executeRequest(String method, String urlString, String payload, 
                                       Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            
            // Set headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            
            // Send payload if provided
            if (payload != null && !payload.isEmpty()) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            // Get response code
            int responseCode = connection.getResponseCode();
            
            // Read the response
            Map<String, String> responseHeaders = new HashMap<>();
            for (String key : connection.getHeaderFields().keySet()) {
                if (key != null) {  // HttpURLConnection returns a key=null for the status line
                    responseHeaders.put(key, connection.getHeaderField(key));
                }
            }
            
            // Get input stream (error stream if response code is >= 400)
            InputStream inputStream;
            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }
            
            // Handle GZIP compression if enabled
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
                inputStream = new GZIPInputStream(inputStream);
            }
            
            // Read response body
            StringBuilder response = new StringBuilder();
            if (inputStream != null) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
            
            return new HttpResponse(responseCode, response.toString(), responseHeaders);
            
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Shutdown the client and its resources
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Example usage
     */
    public static void main(String[] args) {
        // Create a client with custom settings
        HttpClient client = new HttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .maxRetries(2)
                .retryDelay(Duration.ofMillis(500))
                .threadPoolSize(4)
                .withDefaultHeader("X-API-Key", "your-api-key")
                .build();
        
        try {
            // Synchronous request
            String jsonPayload = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
            HttpResponse response = client.post("https://api.example.com/users", jsonPayload);
            
            System.out.println("Response code: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            
            // Asynchronous request
            client.postAsync("https://api.example.com/async", jsonPayload)
                .thenAccept(asyncResponse -> {
                    System.out.println("Async response received: " + asyncResponse.getStatusCode());
                })
                .exceptionally(e -> {
                    System.err.println("Async request failed: " + e.getMessage());
                    return null;
                });
            
            // Allow time for async request to complete
            Thread.sleep(2000);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shutdown the client when done
            client.shutdown();
        }
    }
}


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaComplexObjectProducer {

    private final Producer<String, Interval<Server<Data>>> producer;
    private final String topic;

    public KafkaComplexObjectProducer(String bootstrapServers, String topic, String keytabPrincipal, 
                                      String keytabPath, String roleArn, String eventTopicArn) {
        Properties props = new Properties();
        
        // Basic Kafka producer configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        
        // Use StringSerializer for keys and our custom serializer for complex values
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());
        props.put("serializer.class", Interval.class.getName());
        
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10000000);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        
        // Security configuration
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.kerberos.service.name", "kafka");
        props.put("sasl.mechanism", "AWS_MSK_IAM");
        props.put("ssl.algorithm", "https");
        
        // IAM authentication
        props.put("sasl.jaas.config", 
                "software.amazon.msk.auth.iam.IAMLoginModule required;");
        props.put("classHandler", "security.FabricAdaptorGSINETIAMCallbackHandler");
        
        // Kerberos configuration
        props.put("principal", keytabPrincipal);
        props.put("keytab", keytabPath);
        
        // AWS specific configuration
        props.put("role.arn", roleArn + "/" + eventTopicArn + "_publisher");
        props.put("agent", "TalX");
        props.put("reconnect.ms", 5000);
        props.put("login.retry.backoff.ms", 5000);
        props.put("enable.idempotence", "false");
        
        @SuppressWarnings("unchecked")
        Producer<String, Interval<Server<Data>>> typedProducer = 
            (Producer<String, Interval<Server<Data>>>) (Object) new KafkaProducer<>(props);
        
        this.producer = typedProducer;
        this.topic = topic;
    }

    public void sendEvent(String key, Interval<Server<Data>> message) {
        try {
            ProducerRecord<String, Interval<Server<Data>>> record = 
                new ProducerRecord<>(topic, key, message);
                
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Error sending message: " + exception.getMessage());
                } else {
                    System.out.println("Message sent successfully to topic " + metadata.topic() +
                            " partition " + metadata.partition() +
                            " offset " + metadata.offset());
                }
            }).get();  // Blocking call - remove .get() for async
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        producer.flush();
        producer.close();
    }
}


public class KafkaComplexProducerDemo {
    public static void main(String[] args) {
        // Default configuration values
        String bootstrapServers = "kafka-broker1:9092,kafka-broker2:9092,kafka-broker3:9092";
        String topic = "mytopic";
        String keytabPrincipal = "${keytab.principal}";
        String keytabPath = "${keytab.path}";
        String roleArn = "${kafka.roleArn}";
        String eventTopicArn = "${kafka.eventTopicArn}";

        // Resolve configuration variables from environment
        bootstrapServers = KafkaConfigUtils.resolveVariables(bootstrapServers);
        topic = KafkaConfigUtils.resolveVariables(topic);
        keytabPrincipal = KafkaConfigUtils.resolveVariables(keytabPrincipal);
        keytabPath = KafkaConfigUtils.resolveVariables(keytabPath);
        roleArn = KafkaConfigUtils.resolveVariables(roleArn);
        eventTopicArn = KafkaConfigUtils.resolveVariables(eventTopicArn);

        // Create producer instance
        KafkaComplexObjectProducer producer = new KafkaComplexObjectProducer(
                bootstrapServers, 
                topic, 
                keytabPrincipal, 
                keytabPath, 
                roleArn, 
                eventTopicArn
        );

        try {
            // Create sample data objects
            Data data1 = new Data("temperature", "23.5", System.currentTimeMillis());
            Data data2 = new Data("humidity", "45.2", System.currentTimeMillis());
            
            // Create server objects containing the data
            Server<Data> server1 = new Server<>("srv-001", "app-server-1.example.com", 8080, data1);
            Server<Data> server2 = new Server<>("srv-002", "app-server-2.example.com", 8080, data2);
            
            // Create interval objects wrapping the servers
            long now = System.currentTimeMillis();
            Interval<Server<Data>> interval1 = new Interval<>(server1, now, now + 3600000); // 1 hour interval
            Interval<Server<Data>> interval2 = new Interval<>(server2, now, now + 7200000); // 2 hour interval
            
            // Send the complex objects to Kafka
            producer.sendEvent("server1", interval1);
            producer.sendEvent("server2", interval2);
            
            System.out.println("All complex messages sent successfully");
        } catch (Exception e) {
            System.err.println("Error in the demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always close the producer
            producer.close();
        }
    }
}


stages:
  - build

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository -Xmx4096m"
  CL_PROJECT_DIR: "."  # Adjust if your project uses a subdirectory

cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - .m2/repository/
    - target/

build:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - microdnf install -y fontconfig
    - export JAVA_HOME=/usr/local/jdk-21

    # Detect changed modules
    - |
      if [[ -n "$CI_COMMIT_BEFORE_SHA" ]]; then
        BASE_COMMIT="$CI_COMMIT_BEFORE_SHA"
      else
        BASE_COMMIT=$(git merge-base origin/${CI_MERGE_REQUEST_TARGET_BRANCH_NAME} HEAD)
      fi

      CHANGED_MODULES=$(
        git diff --name-only ${BASE_COMMIT}..HEAD |
        grep -E 'pom\.xml|src/.*' |
        sed -n 's#\(.*\)/pom\.xml#\1#p; s#\(.*/src/.*\)#\1#p' |
        xargs -I{} dirname {} |
        sort -u |
        sed 's#/#:#g' |
        tr '\n' ',' | sed 's/,$//'
      )

    # Build only changed modules + dependencies
    - |
      if [[ -n "$CHANGED_MODULES" ]]; then
        echo "Building changed modules: ${CHANGED_MODULES}"
        mvn -B versions:set -DnewVersion="${VERSION}" -DskipTests
        mvn install -pl "$CHANGED_MODULES" -am -amd -DskipTests -T 1C
      else
        echo "No changes detected. Skipping build."
      fi

    # Copy artifacts only from built modules
    - mkdir -p artifacts
    - mkdir -p serviceArtifacts
    - |
      for module in back-service ls-event-listener feedback-event-listener feedback-batch feedback-goals-app rules-engine security; do
        if [[ -d "${module}/target" ]]; then
          cp ${module}/target/*.jar artifacts/
        fi
      done
    - ls -al artifacts/
