import AppKit
let root="/Users/mac/My Workspace/Wobbrik/docs/reviews/"
func equal(_ a:String,_ b:String,_ region: CGRect? = nil) -> Bool {
 func pixels(_ path:String) -> NSBitmapImageRep {
  let image=NSBitmapImageRep(data:try! Data(contentsOf:URL(fileURLWithPath:path)))!.cgImage!
  return NSBitmapImageRep(cgImage:region == nil ? image : image.cropping(to:region!)!)
 }
 let a=pixels(a),b=pixels(b)
 guard a.pixelsWide==b.pixelsWide && a.pixelsHigh==b.pixelsHigh else {return false}
 for y in 0..<a.pixelsHigh { for x in 0..<a.pixelsWide {if a.colorAt(x:x,y:y) != b.colorAt(x:x,y:y) {return false}}}
 return true
}
let out=root+"purple-catalog-visual/"
var checks=[String:Bool]()
checks["approved base1 template (catalog cell)"]=equal(out+"before/catalog-00.png",out+"after/catalog-00.png",CGRect(x:0,y:0,width:585,height:1266))
for id in [1,9] {
 checks["inventory detail \(id) full-image"]=equal(root+"purple-roof-join-visual/production-detail-\(id)-390-font1.png",out+"after/production-detail-\(id)-390-font1.png")
}
let report=checks.sorted {$0.key<$1.key}.map {"\($0.value ? "PASS" : "FAIL") \($0.key)"}.joined(separator:"\n")
print(report)
try report.write(toFile:out+"parity.txt",atomically:true,encoding:.utf8)
if checks.values.contains(false) {exit(1)}
