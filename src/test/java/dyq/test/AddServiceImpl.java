

package dyq.test;

import dyq.rpc.server.annotation.Rpc;

import java.util.ArrayList;
import java.util.List;

@Rpc(tag = "test")
public class AddServiceImpl implements AddService{
    @Override
    public List<Integer> add(int a, int b) {
        List<Integer> ret = new ArrayList<>();
        ret.add(a);
        ret.add(b);
        return ret;
    }
}
