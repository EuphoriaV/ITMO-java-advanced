package info.kgeorgiy.ja.kuznetsov.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery {
    private final Comparator<Student> COMPARATOR = Comparator.comparing(Student::getLastName).
            thenComparing(Student::getFirstName).reversed().thenComparing(Student::getId);

    private <R> List<R> getListOfFields(Collection<Student> collection, Function<Student, R> function) {
        return collection.stream().map(function).toList();
    }

    private List<Student> sort(Collection<Student> collection, Comparator<Student> comparator) {
        return collection.stream().sorted(comparator).toList();
    }

    private List<Student> findByPredicate(Collection<Student> collection, Predicate<Student> predicate) {
        return sort(collection, COMPARATOR).stream().filter(predicate).toList();
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getListOfFields(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getListOfFields(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getListOfFields(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getListOfFields(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getListOfFields(students, Student::getFirstName));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Comparator.naturalOrder()).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sort(students, Comparator.naturalOrder());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sort(students, COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findByPredicate(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findByPredicate(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findByPredicate(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group).stream().collect(Collectors.toMap(
                Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }
}
