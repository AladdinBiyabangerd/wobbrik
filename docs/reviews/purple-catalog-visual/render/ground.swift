import AppKit
let dir=CommandLine.arguments[1]
let count=try FileManager.default.contentsOfDirectory(atPath:dir).filter {$0.hasPrefix("catalog-") && $0.hasSuffix(".png")}.count
for start in stride(from:0,to:count,by:4) {
 let n=min(4,count-start)
 let rep=NSBitmapImageRep(bitmapDataPlanes:nil,pixelsWide:1170*n,pixelsHigh:600,bitsPerSample:8,samplesPerPixel:4,hasAlpha:true,isPlanar:false,colorSpaceName:.deviceRGB,bytesPerRow:0,bitsPerPixel:0)!
 NSGraphicsContext.saveGraphicsState();NSGraphicsContext.current=NSGraphicsContext(bitmapImageRep:rep)
 for i in 0..<n {
  let src=NSBitmapImageRep(data:try Data(contentsOf:URL(fileURLWithPath:dir+String(format:"/catalog-%02d.png",start+i))))!.cgImage!
  for row in 0..<2 {
   let crop=src.cropping(to:CGRect(x:0,y:row*1266+940,width:1170,height:300))!
   NSImage(cgImage:crop,size:NSSize(width:1170,height:300)).draw(in:NSRect(x:i*1170,y:(1-row)*300,width:1170,height:300))
  }
 }
 NSGraphicsContext.restoreGraphicsState();try rep.representation(using:.png,properties:[:])!.write(to:URL(fileURLWithPath:dir+"/ground-\(start).png"))
}
