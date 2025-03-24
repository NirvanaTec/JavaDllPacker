/*
 * 涅槃科技 and 风横
 * https://npyyds.top/
 * https://gitee.com/newNP/
 * https://github.com/NirvanaTec/
 * 最终解释权归涅槃科技所有，涅槃科技版权所有。
 */
package space;

public class ClassPair implements Comparable<ClassPair> {
    public byte[] classData;
    public int priority;
    public ClassInfo classInfo;

    public ClassPair(byte[] classData) {
        this.classData = classData;
        this.priority = 0;
    }

    public int compareTo(ClassPair o) {
        return o.priority - this.priority;
    }
}
